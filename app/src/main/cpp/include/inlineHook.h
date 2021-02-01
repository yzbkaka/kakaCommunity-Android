#ifndef _INLINEHOOK_H
#define _INLINEHOOK_H

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

enum ELE7EN_STATUS {
    ELE7EN_ERROR_UNKNOWN = -1,
    ELE7EN_OK = 0,
    ELE7EN_ERROR_NOT_INITIALIZED,
    ELE7EN_ERROR_NOT_EXECUTABLE,
    ELE7EN_ERROR_NOT_REGISTERED,
    ELE7EN_ERROR_NOT_HOOKED,
    ELE7EN_ERROR_ALREADY_REGISTERED,
    ELE7EN_ERROR_ALREADY_HOOKED,
    ELE7EN_ERROR_SO_NOT_FOUND,
    ELE7EN_ERROR_FUNCTION_NOT_FOUND
};

enum INLINEHOOK_NUMBER {
    INLINEHOOK_SIZE = 1024,
    INLINEHOOK_ORIGIN_BOUNDARIES_SIZE = 4,
    INLINEHOOK_TRAMPOLINE_BOUNDARIES_SIZE = 20,
    INLINEHOOK_SIXTEEN = 16,
    INLINEHOOK_DIR_PATH_SIZE = 32,
    INLINEHOOK_TWELVE = 12,
    INLINEHOOK_EIGHT = 8,
    INLINEHOOK_DOUBLE = 2,
};

enum ELE7EN_STATUS registerInlineHook(uint32_t target_addr, uint32_t new_addr, uint32_t **proto_addr);
enum ELE7EN_STATUS inlineUnHook(uint32_t target_addr);
void inlineUnHookAll();
enum ELE7EN_STATUS inlineHook(uint32_t target_addr);
void inlineHookAll();

#ifdef __cplusplus
}
#endif

#endif
