// Copyright (c) 2016 avs333
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
//		of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
//		to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//		copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//		The above copyright notice and this permission notice shall be included in all
//		copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// 		AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
//来自第三方仓库，仓库地址：https://github.com/avs333/Nougat_dlfunctions


#include <stdio.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <elf.h>
#include <android/log.h>
#include "fake_dlfcn.h"

#define TAG_NAME	"test2:fake_dlfcn"

#define log_info(fmt,args...) __android_log_print(ANDROID_LOG_INFO, TAG_NAME, (const char *) fmt, ##args)
#define log_err(fmt,args...) __android_log_print(ANDROID_LOG_ERROR, TAG_NAME, (const char *) fmt, ##args)

#define log_dbg log_info

#ifdef __arm__
#define Elf_Ehdr Elf32_Ehdr
#define Elf_Shdr Elf32_Shdr
#define Elf_Sym  Elf32_Sym
#elif defined(__i386__)
#define Elf_Ehdr Elf32_Ehdr
#define Elf_Shdr Elf32_Shdr
#define Elf_Sym  Elf32_Sym
#elif defined(__aarch64__)
#define Elf_Ehdr Elf64_Ehdr
#define Elf_Shdr Elf64_Shdr
#define Elf_Sym  Elf64_Sym
#else
#error "Arch unknown, please port me"
#endif

struct ctx {
    char *load_addr;
    char *dynstr;
    char *dynsym;
    int nsyms;
    off_t bias;
};

#ifdef __cplusplus
extern "C" {
#endif

/**
 * 闭指定句柄的动态链接库
 *
 * @param handle 文件句柄
 * @return 动态链接库计数，只有当值为0时，才会被系统卸载
 */
int fake_dlclose(void *handle) {
    if (handle) {
        struct ctx *ctx = (struct ctx *) handle;
        if (ctx->dynsym) {
            free(ctx->dynsym);
        }
        if (ctx->dynstr) {
            free(ctx->dynstr);
        }
        free(ctx);
    }
    return 0;
}

/**
 * 加载动态链接库
 *
 * @param libpath 路径
 * @param flags 策略
 * @return handle
 */
void *fake_dlopen(const char *libpath, int flags) {
    FILE *maps;
    char buff[256];
    struct ctx *ctx = 0;
    off_t load_addr, size;
    int k, fd = -1, found = 0;
    char *shoff;
    Elf_Ehdr *elf = (Elf_Ehdr *) MAP_FAILED;

#define fatal(fmt, args...) do { log_err(fmt,##args); goto err_exit; } while(0)

    maps = fopen("/proc/self/maps", "r");
    if (!maps) fatal("failed to open maps");
    while (fgets(buff, sizeof(buff), maps)) {
        if ((strstr(buff, "r-xp") || strstr(buff, "r--p")) && strstr(buff, libpath)) {
            found = 1;
            break;
        }
    }
    fclose(maps);
    if (!found) {
        fatal("%s not found in my userspace", libpath);
    }
    if (sscanf(buff, "%lx", &load_addr) != 1) {
        fatal("failed to read load address for %s", libpath);
    }
    log_info("%s loaded in Android at 0x%08lx", libpath, load_addr);
    fd = open(libpath, O_RDONLY);
    if (fd < 0) {
        fatal("failed to open %s", libpath);
    }
    size = lseek(fd, 0, SEEK_END);
    if (size <= 0) {
        fatal("lseek() failed for %s", libpath);
    }
    elf = (Elf_Ehdr *) mmap(0, size, PROT_READ, MAP_SHARED, fd, 0);
    close(fd);
    fd = -1;
    if (elf == MAP_FAILED) {
        fatal("mmap() failed for %s", libpath);
    }
    ctx = (struct ctx *) calloc(1, sizeof(struct ctx));
    if (!ctx) {
        fatal("no memory for %s", libpath);
    }
    ctx->load_addr = (char *) load_addr;
    shoff = ((char *) elf) + elf->e_shoff;
    for (k = 0; k < elf->e_shnum; k++, shoff += elf->e_shentsize) {
        Elf_Shdr *sh = (Elf_Shdr *) shoff;
        log_dbg("%s: k=%d shdr=%p type=%x", __func__, k, sh, sh->sh_type);
        switch (sh->sh_type) {
            case SHT_DYNSYM:
                // .dynsym
                if (ctx->dynsym) {
                    fatal("%s: duplicate DYNSYM sections", libpath);
                }
                ctx->dynsym = (char*) malloc(sh->sh_size);
                if (!ctx->dynsym) {
                    fatal("%s: no memory for .dynsym", libpath);
                }
                memcpy(ctx->dynsym, ((char *) elf) + sh->sh_offset, sh->sh_size);
                ctx->nsyms = (sh->sh_size / sizeof(Elf_Sym));
                break;
            case SHT_STRTAB:
                // .dynstr保证是第一个STRTAB
                if (ctx->dynstr) break;
                ctx->dynstr = (char*) malloc(sh->sh_size);
                if (!ctx->dynstr) {
                    fatal("%s: no memory for .dynstr", libpath);
                }
                memcpy(ctx->dynstr, ((char *) elf) + sh->sh_offset, sh->sh_size);
                break;
            case SHT_PROGBITS:
                if (!ctx->dynstr || !ctx->dynsym) break;
                // 不用检查节名
                ctx->bias = (off_t) sh->sh_addr - (off_t) sh->sh_offset;
                k = elf->e_shnum;
                break;
        }
    }
    munmap(elf, size);
    elf = 0;
    if (!ctx->dynstr || !ctx->dynsym) {
        fatal("dynamic sections not found in %s", libpath);
    }

#undef fatal
    log_dbg("%s: ok, dynsym = %p, dynstr = %p", libpath, ctx->dynsym, ctx->dynstr);
    return ctx;
    err_exit:
    if (fd >= 0) {
        close(fd);
    }
    if (elf != MAP_FAILED) {
        munmap(elf, size);
    }
    fake_dlclose(ctx);
    return 0;
}

/**
 * 根据handle加载动态库
 *
 * @param handle 句柄
 * @param name 函数名称
 * @return 函数开始地址
 */
void *fake_dlsym(void *handle, const char *name) {
    int k;
    struct ctx *ctx = (struct ctx *) handle;
    Elf_Sym *sym = (Elf_Sym *) ctx->dynsym;
    char *strings = (char *) ctx->dynstr;
    for (k = 0; k < ctx->nsyms; k++, sym++)
        if (strcmp(strings + sym->st_name, name) == 0) {
            // sym->st_value是可重定位部分的偏移量，但VMA共享库或exe文件，所以必须减去偏差
            void *ret = ctx->load_addr + sym->st_value - ctx->bias;
            log_info("%s found at %p", name, ret);
            return ret;
        }
    return 0;
}
#ifdef __cplusplus
}
#endif

