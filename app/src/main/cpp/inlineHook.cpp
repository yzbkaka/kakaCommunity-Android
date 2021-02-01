/*
thumb16 thumb32 arm32 inlineHook
author: ele7enxxh
mail: ele7enxxh@qq.com
website: ele7enxxh.com
modified time: 2015-01-23
created time: 2015-11-30
来自第三方仓库，仓库地址：https://github.com/ele7enxxh/Android-Inline-Hook
*/

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <dirent.h>
#include <signal.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <sys/ptrace.h>

#include "relocate.h"
#include "include/inlineHook.h"

#include <android/log.h>
#include <unistd.h>

#define  LOG_TAG    "inlinehook"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


#ifndef PAGE_SIZE
#define PAGE_SIZE 4096
#endif

#define PAGE_START(addr)	(~(PAGE_SIZE - 1) & (addr))
#define SET_BIT0(addr)		(addr | 1)
#define CLEAR_BIT0(addr)	(addr & 0xFFFFFFFE)
#define TEST_BIT0(addr)		(addr & 1)

#define ACTION_ENABLE	0
#define ACTION_DISABLE	1

extern "C" {
enum HOOK_STATUS {
	REGISTERED,
	HOOKED,
};


struct InlineHookItem {
	uint32_t target_addr;
	uint32_t new_addr;
	uint32_t **proto_addr;
	void *orig_instructions;
	int orig_boundaries[INLINEHOOK_ORIGIN_BOUNDARIES_SIZE];
	int trampoline_boundaries[INLINEHOOK_TRAMPOLINE_BOUNDARIES_SIZE];
	int count;
	void *trampoline_instructions;
	int length;
	int status;
	int mode;
};

struct InlineHookInfo {
	struct InlineHookItem item[INLINEHOOK_SIZE];
	int size;
};

static struct InlineHookInfo info = {0};

/**
 * 得到所有的线程id
 *
 * @param exclude_tid 加载的进程号
 * @param tids  进程号
 * @return 线程的数量
 */
static int getAllTids(pid_t exclude_tid, pid_t *tids) {
	char dir_path[INLINEHOOK_DIR_PATH_SIZE];
	DIR *dir;
	int i;
	struct dirent *entry;
	pid_t tid;
	if (exclude_tid < 0) {
		snprintf(dir_path, sizeof(dir_path), "/proc/self/task");
	}
	else {
		snprintf(dir_path, sizeof(dir_path), "/proc/%d/task", exclude_tid);
	}
	dir = opendir(dir_path);
	if (dir == NULL) {
		return 0;
	}
	i = 0;
	while((entry = readdir(dir)) != NULL) {
		tid = atoi(entry->d_name);
		if (tid != 0 && tid != exclude_tid) {
			tids[i++] = tid;
		}
	}
	closedir(dir);
	return i;
}

/**
 * 判断是否是当前进程
 *
 * @param item 自定义函数原型结构体
 * @param regs
 * @param action
 * @return 判断
 */
static bool doProcessThreadPC(struct InlineHookItem *item, struct pt_regs *regs, int action) {
	int offset;
	int i;
	switch (action) {
		case ACTION_ENABLE:
			offset = regs->ARM_pc - CLEAR_BIT0(item->target_addr);
			for (i = 0; i < item->count; ++i) {
				if (offset == item->orig_boundaries[i]) {
					regs->ARM_pc = (uint32_t) item->trampoline_instructions + item->trampoline_boundaries[i];
					return true;
				}
			}
			break;
		case ACTION_DISABLE:
			offset = regs->ARM_pc - (int) item->trampoline_instructions;
			for (i = 0; i < item->count; ++i) {
				if (offset == item->trampoline_boundaries[i]) {
					regs->ARM_pc = CLEAR_BIT0(item->target_addr) + item->orig_boundaries[i];
					return true;
				}
			}
			break;
	}
	return false;
}

static void processThreadPC(pid_t tid, struct InlineHookItem *item, int action) {
	struct pt_regs regs;
	if (ptrace(PTRACE_GETREGS, tid, NULL, &regs) == 0) {
		if (item == NULL) {
			int pos;
			for (pos = 0; pos < info.size; ++pos) {
				if (doProcessThreadPC(&info.item[pos], &regs, action) == true) {
					break;
				}
			}
		}
		else {
			doProcessThreadPC(item, &regs, action);
		}
		ptrace(PTRACE_SETREGS, tid, NULL, &regs);
	}
}

static pid_t freeze(struct InlineHookItem *item, int action) {
	int count;
	pid_t tids[INLINEHOOK_SIZE];
	pid_t pid;
	pid = -1;
	count = getAllTids(gettid(), tids);
	if (count > 0) {
		pid = fork();
		if (pid == 0) {
			int i;
			for (i = 0; i < count; ++i) {
				if (ptrace(PTRACE_ATTACH, tids[i], NULL, NULL) == 0) {
					waitpid(tids[i], NULL, WUNTRACED);
					processThreadPC(tids[i], item, action);
				}
			}
			raise(SIGSTOP);
			for (i = 0; i < count; ++i) {
				ptrace(PTRACE_DETACH, tids[i], NULL, NULL);
			}
			raise(SIGKILL);
		}
		else if (pid > 0) {
			waitpid(pid, NULL, WUNTRACED);
		}
	}
	return pid;
}

static void unFreeze(pid_t pid) {
	if (pid < 0) {
		return;
	}
	kill(pid, SIGCONT);
	wait(NULL);
}

/**
 * 判断是否是16进制
 *
 * @param s 传入的char
 * @return  判断结果
 */
bool isHexDigital(char* s) {
	if (s == NULL) {
		return false;
	}
	int len = strlen(s);
	if (len > 0) {
		for (int i = 0; i < len; i++) {
			if ((s[i] >= '0' && s[i] <= '9') ||
				(s[i] >= 'a' && s[i] <= 'f') ||
				(s[i] >= 'A' && s[i] <= 'F')) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	return false;
}

/**
 * 判断是否是可执行地址
 *
 * @param addr 地址
 * @return 判断结果
 */
static bool isExecutableAddr(uint32_t addr) {
	FILE *fp;
	char line[INLINEHOOK_SIZE];
	uint32_t start;
	uint32_t end;
	char *p;
	fp = fopen("/proc/self/maps", "r");
	if (fp == NULL) {
		return false;
	}
	while (fgets(line, sizeof(line), fp)) {
		if (strstr(line, "r-xp") || strstr(line, "rwxp")) {
			char *head = strtok(line, "-");
			char *tail = strtok(NULL, " ");
			if (!isHexDigital(head) || !isHexDigital(tail)) {
				fclose(fp);
				return false;
			}
			start = strtoul(head, &p, INLINEHOOK_SIXTEEN);
			end = strtoul(tail, &p, INLINEHOOK_SIXTEEN);
			if (addr >= start && addr <= end) {
				fclose(fp);
				return true;
			}
		}
	}
	fclose(fp);
	return false;
}

/**
 * 方法检查是否已经有相关记录
 *
 * @param target_addr 目标函数地址
 * @return 该hook函数自定义的结构体
 */
static struct InlineHookItem *findInlineHookItem(uint32_t target_addr) {
	int i;
	for (i = 0; i < info.size; ++i) {
		if (info.item[i].target_addr == target_addr) {
			return &info.item[i];
		}
	}
	return NULL;
}

/**
 * 为指定的函数地址创建一个新的inLineHookItem
 *
 * @return inLineHookItem 自定义函数结构体
 */
static struct InlineHookItem *addInlineHookItem() {
	struct InlineHookItem *item;
	LOGI("addInlineHookItem info.size:%d", info.size);
	if (info.size >= INLINEHOOK_SIZE) {
		LOGE("addInlineHookItem return NULL");
		return NULL;
	}
	item = &info.item[info.size];
	++info.size;
	return item;
}

/**
 * 删除定函数结构体
 *
 * @param pos 具体的下标
 */
static void deleteInlineHookItem(int pos) {
	info.item[pos] = info.item[info.size - 1];
	--info.size;
}

/**
 * 注册InLineHook，进行初始化
 *
 * @param target_addr 目标函数地址
 * @param new_addr hook后的新函数地址
 * @param proto_addr 函数原型
 * @return  结构体中定义的状态
 */
enum ELE7EN_STATUS registerInlineHook(uint32_t target_addr, uint32_t new_addr, uint32_t **proto_addr) {
	struct InlineHookItem *item;
	LOGI("registerInlineHook target_addr:%p, new_addr:%p, proto_addr:%p", (void*)target_addr,  (void*)new_addr, proto_addr);
	if (!isExecutableAddr(target_addr) || !isExecutableAddr(new_addr)) {
		LOGE("registerInlineHook NOT_EXECUTABLE");
		return ELE7EN_ERROR_NOT_EXECUTABLE;
	}
	item = findInlineHookItem(target_addr);
	if (item != NULL) {
		if (item->status == REGISTERED) {
			LOGE("registerInlineHook ELE7EN_ERROR_ALREADY_REGISTERED");
			return ELE7EN_ERROR_ALREADY_REGISTERED;
		}
		else if (item->status == HOOKED) {
			LOGE("registerInlineHook ELE7EN_ERROR_ALREADY_HOOKED");
			return ELE7EN_ERROR_ALREADY_HOOKED;
		}
		else {
			LOGE("registerInlineHook ELE7EN_ERROR_UNKNOWN");
			return ELE7EN_ERROR_UNKNOWN;
		}
	}
	else {
		LOGE("registerInlineHook target_addr item is NULL");
	}
	item = addInlineHookItem();
	item->target_addr = target_addr;
	item->new_addr = new_addr;
	item->proto_addr = proto_addr;
	item->length = TEST_BIT0(item->target_addr) ? INLINEHOOK_TWELVE : INLINEHOOK_EIGHT;
	item->orig_instructions = malloc(item->length);
	memcpy(item->orig_instructions, (void *) CLEAR_BIT0(item->target_addr), item->length);
	item->trampoline_instructions = mmap(NULL, PAGE_SIZE, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_ANONYMOUS | MAP_PRIVATE, 0, 0);
	relocateInstruction(item->target_addr, item->orig_instructions, item->length, item->trampoline_instructions, item->orig_boundaries, item->trampoline_boundaries, &item->count);
	item->status = REGISTERED;
	LOGI("registerInlineHook item->target_addr:%p, target_addr:%p, new_addr:%p, proto_addr:%p",  (void*)item->target_addr,  (void*)target_addr,  (void*)new_addr, proto_addr);
	return ELE7EN_OK;
}

/**
 * 进行取消hook操作
 *
 * @param item 函数结构体
 * @param pos 具体位置
 */
static void doInlineUnHook(struct InlineHookItem *item, int pos) {
	mprotect((void *) PAGE_START(CLEAR_BIT0(item->target_addr)), PAGE_SIZE * INLINEHOOK_DOUBLE, PROT_READ | PROT_WRITE | PROT_EXEC);
	memcpy((void *) CLEAR_BIT0(item->target_addr), item->orig_instructions, item->length);
	mprotect((void *) PAGE_START(CLEAR_BIT0(item->target_addr)), PAGE_SIZE * INLINEHOOK_DOUBLE, PROT_READ | PROT_EXEC);
	munmap(item->trampoline_instructions, PAGE_SIZE);
	free(item->orig_instructions);
	deleteInlineHookItem(pos);
	cacheflush(CLEAR_BIT0(item->target_addr), CLEAR_BIT0(item->target_addr) + item->length, 0);
}

enum ELE7EN_STATUS inlineUnHook(uint32_t target_addr) {
	int i;
	for (i = 0; i < info.size; ++i) {
		if (info.item[i].target_addr == target_addr && info.item[i].status == HOOKED) {
			pid_t pid;
			pid = freeze(&info.item[i], ACTION_DISABLE);
			doInlineUnHook(&info.item[i], i);
			unFreeze(pid);
			return ELE7EN_OK;
		}
	}
	return ELE7EN_ERROR_NOT_HOOKED;
}

void inlineUnHookAll() {
	pid_t pid;
	int i;
	pid = freeze(NULL, ACTION_DISABLE);
	for (i = 0; i < info.size; ++i) {
		if (info.item[i].status == HOOKED) {
			doInlineUnHook(&info.item[i], i);
			--i;
		}
	}
	unFreeze(pid);
}

/**
 * 处理对原始方法的访问并修改原始方法，写入跳转指令
 *
 * @param item 自定义的函数结构体
 */
static void doInlineHook(struct InlineHookItem *item) {
	mprotect((void *) PAGE_START(CLEAR_BIT0(item->target_addr)), PAGE_SIZE * INLINEHOOK_DOUBLE, PROT_READ | PROT_WRITE | PROT_EXEC);
	if (item->proto_addr != NULL) {
		*(item->proto_addr) = TEST_BIT0(item->target_addr) ? (uint32_t *) SET_BIT0((uint32_t) item->trampoline_instructions) : (uint32_t *) item->trampoline_instructions;
	}
	if (TEST_BIT0(item->target_addr)) {
		int i;
		i = 0;
		if (CLEAR_BIT0(item->target_addr) % INLINEHOOK_ORIGIN_BOUNDARIES_SIZE != 0) {
			((uint16_t *) CLEAR_BIT0(item->target_addr))[i++] = 0xBF00;  // NOP
		}
		((uint16_t *) CLEAR_BIT0(item->target_addr))[i++] = 0xF8DF;
		((uint16_t *) CLEAR_BIT0(item->target_addr))[i++] = 0xF000;	// LDR.W PC, [PC]
		((uint16_t *) CLEAR_BIT0(item->target_addr))[i++] = item->new_addr & 0xFFFF;
		((uint16_t *) CLEAR_BIT0(item->target_addr))[i++] = item->new_addr >> INLINEHOOK_SIXTEEN;
	}
	else {
		((uint32_t *) (item->target_addr))[0] = 0xe51ff004;	// LDR PC, [PC, #-4]
		((uint32_t *) (item->target_addr))[1] = item->new_addr;
	}
	mprotect((void *) PAGE_START(CLEAR_BIT0(item->target_addr)), PAGE_SIZE * INLINEHOOK_DOUBLE, PROT_READ | PROT_EXEC);
	item->status = HOOKED;
	cacheflush(CLEAR_BIT0(item->target_addr), CLEAR_BIT0(item->target_addr) + item->length, 0);
}

enum ELE7EN_STATUS inlineHook(uint32_t target_addr) {
	int i;
	struct InlineHookItem *item;
	item = NULL;
	LOGI("inlineHook info:%p, size:%d, target_addr:%p", &info, info.size,  (void*)target_addr);
	for (i = 0; i < info.size; ++i) {
		LOGI("inlineHook info.target_addr:%p, target_addr:%p",  (void*)info.item[i].target_addr,  (void*)target_addr);
		if (info.item[i].target_addr == target_addr) {
			item = &info.item[i];
			break;
		}
	}
	if (item == NULL) {
		LOGE("inlineHook ELE7EN_ERROR_NOT_REGISTERED");
		return ELE7EN_ERROR_NOT_REGISTERED;
	}
	if (item->status == REGISTERED) {
		pid_t pid;
		//pid = freeze(item, ACTION_ENABLE);
		doInlineHook(item);
		//unFreeze(pid);
		LOGE("inlineHook OK!!");
		return ELE7EN_OK;
	}
	else if (item->status == HOOKED) {
		LOGE("inlineHook ELE7EN_ERROR_ALREADY_HOOKED");
		return ELE7EN_ERROR_ALREADY_HOOKED;
	}
	else {
		LOGE("inlineHook ELE7EN_ERROR_UNKNOWN");
		return ELE7EN_ERROR_UNKNOWN;
	}
}

void inlineHookAll() {
	pid_t pid;
	int i;
	pid = freeze(NULL, ACTION_ENABLE);
	for (i = 0; i < info.size; ++i) {
		if (info.item[i].status == REGISTERED) {
			doInlineHook(&info.item[i]);
		}
	}
	unFreeze(pid);
}

}