/*
relocate instruction
author: ele7enxxh
mail: ele7enxxh@qq.com
website: ele7enxxh.com
modified time: 2016-10-17
created time: 2015-01-17
来自第三方仓库，仓库地址：https://github.com/ele7enxxh/Android-Inline-Hook
*/
#ifndef _RELOCATE_H
#define _RELOCATE_H
#ifdef __cplusplus
extern "C" {
#endif
#include <stdio.h>
void relocateInstruction(uint32_t target_addr, void *orig_instructions, int length,
                         void *trampoline_instructions, int *orig_boundaries,
                         int *trampoline_boundaries, int *count);
enum RELOCATE_NUMBER {
    RELOCATE_TWO = 2,
    RELOCATE_THREE = 3,
    RELOCATE_FOUR = 4,
    RELOCATE_FIVE = 5,
    RELOCATE_SIX = 6,
    RELOCATE_SEVEN = 7,
    RELOCATE_EIGHT = 8,
    RELOCATE_TEN = 10,
    RELOCATE_ELEVEN = 11,
    RELOCATE_TWELVE = 12,
    RELOCATE_THIRTEEN = 13,
    RELOCATE_FOURTEEN = 14,
    RELOCATE_SIXTEEN = 16,
    RELOCATE_TWENTY_TWO = 22,
    RELOCATE_TWENTY_THREE = 23,
    RELOCATE_TWENTY_FOUR = 24,
    RELOCATE_TWENTY_FIVE = 25,
    RELOCATE_TWENTY_SIX = 26,
};
#endif
#ifdef __cplusplus
}
#endif