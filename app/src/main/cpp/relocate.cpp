/*
relocate instruction
author: ele7enxxh
mail: ele7enxxh@qq.com
website: ele7enxxh.com
modified time: 2016-10-17
created time: 2015-01-17
来自第三方仓库，仓库地址：https://github.com/ele7enxxh/Android-Inline-Hook
*/

#include "relocate.h"
#define ALIGN_PC(pc)	(pc & 0xFFFFFFFC)
extern "C" {
enum INSTRUCTION_TYPE {
	// B <label>
	B1_THUMB16,
	// B <label>
	B2_THUMB16,
	// BX PC
	BX_THUMB16,
	// ADD <Rdn>, PC (Rd != PC, Rn != PC) 在对ADD进行修正时，采用了替换PC为Rr的方法，当Rd也为PC时，由于之前更改了Rr的值，可能会影响跳转后的正常功能。
	ADD_THUMB16,
	// MOV Rd, PC
	MOV_THUMB16,
	// ADR Rd, <label>
	ADR_THUMB16,
	// LDR Rt, <label>
	LDR_THUMB16,
	// CB{N}Z <Rn>, <label>
	CB_THUMB16,
	// BLX <label>
	BLX_THUMB32,
	// BL <label>
	BL_THUMB32,
	// B.W <label>
	B1_THUMB32,
	// B.W <label>
	B2_THUMB32,
	// ADR.W Rd, <label>
	ADR1_THUMB32,
	// ADR.W Rd, <label>
	ADR2_THUMB32,
	// LDR.W Rt, <label>
	LDR_THUMB32,
	// TBB [PC, Rm]
	TBB_THUMB32,
	// TBH [PC, Rm, LSL #1]
	TBH_THUMB32,
	// BLX <label>
	BLX_ARM,
	// BL <label>
	BL_ARM,
	// B <label>
	B_ARM,
	// BX PC
	BX_ARM,
	// ADD Rd, PC, Rm (Rd != PC, Rm != PC) 在对ADD进行修正时，采用了替换PC为Rr的方法，当Rd也为PC时，由于之前更改了Rr的值，可能会影响跳转后的正常功能;实际汇编中没有发现Rm也为PC的情况，故未做处理。
	ADD_ARM,
	// ADR Rd, <label>
	ADR1_ARM,
	// ADR Rd, <label>
	ADR2_ARM,
	// MOV Rd, PC
	MOV_ARM,
	// LDR Rt, <label>
	LDR_ARM,
	UNDEFINE,
};
/**
 * 在Thumb16模式下获得指令类型
 *
 * @param instruction
 * @return 指令类型
 */
static int getTypeInThumb16(uint16_t instruction) {
	if ((instruction & 0xF000) == 0xD000) {
		return B1_THUMB16;
	}
	if ((instruction & 0xF800) == 0xE000) {
		return B2_THUMB16;
	}
	if ((instruction & 0xFFF8) == 0x4778) {
		return BX_THUMB16;
	}
	if ((instruction & 0xFF78) == 0x4478) {
		return ADD_THUMB16;
	}
	if ((instruction & 0xFF78) == 0x4678) {
		return MOV_THUMB16;
	}
	if ((instruction & 0xF800) == 0xA000) {
		return ADR_THUMB16;
	}
	if ((instruction & 0xF800) == 0x4800) {
		return LDR_THUMB16;
	}
	if ((instruction & 0xF500) == 0xB100) {
		return CB_THUMB16;
	}
	return UNDEFINE;
}
/**
 * 在Thumb32模式下获得指令类型
 *
 * @param instruction
 * @return 指令类型
 */
static int getTypeInThumb32(uint32_t instruction) {
	if ((instruction & 0xF800D000) == 0xF000C000) {
		return BLX_THUMB32;
	}
	if ((instruction & 0xF800D000) == 0xF000D000) {
		return BL_THUMB32;
	}
	if ((instruction & 0xF800D000) == 0xF0008000) {
		return B1_THUMB32;
	}
	if ((instruction & 0xF800D000) == 0xF0009000) {
		return B2_THUMB32;
	}
	if ((instruction & 0xFBFF8000) == 0xF2AF0000) {
		return ADR1_THUMB32;
	}
	if ((instruction & 0xFBFF8000) == 0xF20F0000) {
		return ADR2_THUMB32;
	}
	if ((instruction & 0xFF7F0000) == 0xF85F0000) {
		return LDR_THUMB32;
	}
	if ((instruction & 0xFFFF00F0) == 0xE8DF0000) {
		return TBB_THUMB32;
	}
	if ((instruction & 0xFFFF00F0) == 0xE8DF0010) {
		return TBH_THUMB32;
	}
	return UNDEFINE;
}
/**
 * 获得ARM模式下的指令类型
 *
 * @param instruction
 * @return 指令类型
 */
static int getTypeInArm(uint32_t instruction) {
	if ((instruction & 0xFE000000) == 0xFA000000) {
		return BLX_ARM;
	}
	if ((instruction & 0xF000000) == 0xB000000) {
		return BL_ARM;
	}
	if ((instruction & 0xF000000) == 0xA000000) {
		return B_ARM;
	}
	if ((instruction & 0xFF000FF) == 0x120001F) {
		return BX_ARM;
	}
	if ((instruction & 0xFEF0010) == 0x8F0000) {
		return ADD_ARM;
	}
	if ((instruction & 0xFFF0000) == 0x28F0000) {
		return ADR1_ARM;
	}
	if ((instruction & 0xFFF0000) == 0x24F0000) {
		return ADR2_ARM;
	}
	if ((instruction & 0xE5F0000) == 0x41F0000) {
		return LDR_ARM;
	}
	if ((instruction & 0xFE00FFF) == 0x1A0000F) {
		return MOV_ARM;
	}
	return UNDEFINE;
}
/**
 * 重定位Thumb16中的指令
 *
 * @param pc pc类型
 * @param instruction 原指令
 * @param trampoline_instructions tarmpoline指令
 * @return 重定位后的指令地址
 */
static int relocateInstructionInThumb16(uint32_t pc, uint16_t instruction, uint16_t *trampoline_instructions) {
	int type;
	int offset;
	type = getTypeInThumb16(instruction);
	if (type == B1_THUMB16 || type == B2_THUMB16 || type == BX_THUMB16) {
		uint32_t x;
		int top_bit;
		uint32_t imm32;
		uint32_t value;
		int idx;
		idx = 0;
		if (type == B1_THUMB16) {
			x = (instruction & 0xFF) << 1;
			top_bit = x >> RELOCATE_EIGHT;
			imm32 = top_bit ? (x | (0xFFFFFFFF << RELOCATE_EIGHT)) : x;
			value = pc + imm32;
			trampoline_instructions[idx++] = instruction & 0xFF00;
			// B PC, #6
			trampoline_instructions[idx++] = 0xE003;
		}
		else if (type == B2_THUMB16) {
			x = (instruction & 0x7FF) << 1;
			top_bit = x >> RELOCATE_ELEVEN;
			imm32 = top_bit ? (x | (0xFFFFFFFF << RELOCATE_ELEVEN)) : x;
			value = pc + imm32;
		}
		else if (type == BX_THUMB16) {
			value = pc;
		}
		// thumb
		value |= 1;
		trampoline_instructions[idx++] = 0xF8DF;
		// LDR.W PC, [PC]
		trampoline_instructions[idx++] = 0xF000;
		trampoline_instructions[idx++] = value & 0xFFFF;
		trampoline_instructions[idx++] = value >> RELOCATE_SIXTEEN;
		offset = idx;
	}
	else if (type == ADD_THUMB16) {
		int rdn;
		int rm;
		int r;
		rdn = ((instruction & 0x80) >> RELOCATE_FOUR) | (instruction & 0x7);
		for (r = RELOCATE_SEVEN; ; --r) {
			if (r != rdn) {
				break;
			}
		}
		// PUSH {Rr}
		trampoline_instructions[0] = 0xB400 | (1 << r);
		// LDR Rr, [PC, #8]
		trampoline_instructions[1] = 0x4802 | (r << RELOCATE_EIGHT);
		trampoline_instructions[2] = (instruction & 0xFF87) | (r << RELOCATE_THREE);
		// POP {Rr}
		trampoline_instructions[RELOCATE_THREE] = 0xBC00 | (1 << r);
		// B PC, #4
		trampoline_instructions[RELOCATE_FOUR] = 0xE002;
		trampoline_instructions[RELOCATE_FIVE] = 0xBF00;
		trampoline_instructions[RELOCATE_SIX] = pc & 0xFFFF;
		trampoline_instructions[RELOCATE_SEVEN] = pc >> RELOCATE_SIXTEEN;
		offset = RELOCATE_EIGHT;
	}
	else if (type == MOV_THUMB16 || type == ADR_THUMB16 || type == LDR_THUMB16) {
		int r;
		uint32_t value;
		if (type == MOV_THUMB16) {
			r = instruction & 0x7;
			value = pc;
		}
		else if (type == ADR_THUMB16) {
			r = (instruction & 0x700) >> RELOCATE_EIGHT;
			value = ALIGN_PC(pc) + (instruction & 0xFF) << RELOCATE_TWO;
		}
		else {
			r = (instruction & 0x700) >> RELOCATE_EIGHT;
			value = ((uint32_t *) (ALIGN_PC(pc) + ((instruction & 0xFF) << RELOCATE_TWO)))[0];
		}
		// LDR Rd, [PC]
		trampoline_instructions[0] = 0x4800 | (r << RELOCATE_EIGHT);
		// B PC, #2
		trampoline_instructions[1] = 0xE001;
		trampoline_instructions[2] = value & 0xFFFF;
		trampoline_instructions[RELOCATE_THREE] = value >> RELOCATE_SIXTEEN;
		offset = RELOCATE_FOUR;
	}
	else if (type == CB_THUMB16) {
		int nonzero;
		uint32_t imm32;
		uint32_t value;
		nonzero = (instruction & 0x800) >> RELOCATE_ELEVEN;
		imm32 = ((instruction & 0x200) >> RELOCATE_THREE) | ((instruction & 0xF8) >> RELOCATE_TWO);
		value = pc + imm32 + 1;
		trampoline_instructions[0] = instruction & 0xFD07;
		// B PC, #6
		trampoline_instructions[1] = 0xE003;
		trampoline_instructions[RELOCATE_TWO] = 0xF8DF;
		trampoline_instructions[RELOCATE_THREE] = 0xF000;
		// LDR.W PC, [PC]
		trampoline_instructions[RELOCATE_FOUR] = value & 0xFFFF;
		trampoline_instructions[RELOCATE_FIVE] = value >> RELOCATE_SIXTEEN;
		offset = RELOCATE_SIX;
	}
	else {
		trampoline_instructions[0] = instruction;
		// NOP
		trampoline_instructions[1] = 0xBF00;
		offset = RELOCATE_TWO;
	}
	return offset;
}
/**
 * 重定位Thumb32中的指令
 *
 * @param pc pc类型
 * @param high_instruction 原指令
 * @param low_instruction 低位指令
 * @param trampoline_instructions tarmpoline指令
 * @return 重定向后的指令地址
 */
static int relocateInstructionInThumb32(uint32_t pc, uint16_t high_instruction, uint16_t low_instruction, uint16_t *trampoline_instructions) {
	uint32_t instruction;
	int type;
	int idx;
	int offset;
	instruction = (high_instruction << RELOCATE_SIXTEEN) | low_instruction;
	type = getTypeInThumb32(instruction);
	idx = 0;
	if (type == BLX_THUMB32 || type == BL_THUMB32 || type == B1_THUMB32 || type == B2_THUMB32) {
		uint32_t j1;
		uint32_t j2;
		uint32_t s;
		uint32_t i1;
		uint32_t i2;
		uint32_t x;
		uint32_t imm32;
		uint32_t value;
		j1 = (low_instruction & 0x2000) >> RELOCATE_THIRTEEN;
		j2 = (low_instruction & 0x800) >> RELOCATE_ELEVEN;
		s = (high_instruction & 0x400) >> RELOCATE_TEN;
		i1 = !(j1 ^ s);
		i2 = !(j2 ^ s);
		if (type == BLX_THUMB32 || type == BL_THUMB32) {
			trampoline_instructions[idx++] = 0xF20F;
			// ADD.W LR, PC, #9
			trampoline_instructions[idx++] = 0x0E09;
		}
		else if (type == B1_THUMB32) {
			trampoline_instructions[idx++] = 0xD000 | ((high_instruction & 0x3C0) << RELOCATE_TWO);
			// B PC, #6
			trampoline_instructions[idx++] = 0xE003;
		}
		trampoline_instructions[idx++] = 0xF8DF;
		// LDR.W PC, [PC]
		trampoline_instructions[idx++] = 0xF000;
		if (type == BLX_THUMB32) {
			x = (s << RELOCATE_TWENTY_FOUR) | (i1 << RELOCATE_TWENTY_THREE) | (i2 << RELOCATE_TWENTY_TWO) | ((high_instruction & 0x3FF) << RELOCATE_TWELVE) | ((low_instruction & 0x7FE) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << RELOCATE_TWENTY_FIVE)) : x;
			value = pc + imm32;
		}
		else if (type == BL_THUMB32) {
			x = (s << RELOCATE_TWENTY_FOUR) | (i1 << RELOCATE_TWENTY_THREE) | (i2 << RELOCATE_TWENTY_TWO) | ((high_instruction & 0x3FF) << RELOCATE_TWELVE) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << RELOCATE_TWENTY_FIVE)) : x;
			value = pc + imm32 + 1;
		}
		else if (type == B1_THUMB32) {
			x = (s << 20) | (j2 << 19) | (j1 << 18) | ((high_instruction & 0x3F) << RELOCATE_TWELVE) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << 21)) : x;
			value = pc + imm32 + 1;
		}
		else if (type == B2_THUMB32) {
			x = (s << RELOCATE_TWENTY_FOUR) | (i1 << RELOCATE_TWENTY_THREE) | (i2 << RELOCATE_TWENTY_TWO) | ((high_instruction & 0x3FF) << RELOCATE_TWELVE) | ((low_instruction & 0x7FF) << 1);
			imm32 = s ? (x | (0xFFFFFFFF << RELOCATE_TWENTY_FIVE)) : x;
			value = pc + imm32 + 1;
		}
		trampoline_instructions[idx++] = value & 0xFFFF;
		trampoline_instructions[idx++] = value >> RELOCATE_SIXTEEN;
		offset = idx;
	}
	else if (type == ADR1_THUMB32 || type == ADR2_THUMB32 || type == LDR_THUMB32) {
		int r;
		uint32_t imm32;
		uint32_t value;
		if (type == ADR1_THUMB32 || type == ADR2_THUMB32) {
			uint32_t i;
			uint32_t imm3;
			uint32_t imm8;
			r = (low_instruction & 0xF00) >> RELOCATE_EIGHT;
			i = (high_instruction & 0x400) >> RELOCATE_TEN;
			imm3 = (low_instruction & 0x7000) >> RELOCATE_TWELVE;
			imm8 = instruction & 0xFF;
			imm32 = (i << 31) | (imm3 << 30) | (imm8 << 27);
			if (type == ADR1_THUMB32) {
				value = ALIGN_PC(pc) + imm32;
			}
			else {
				value = ALIGN_PC(pc) - imm32;
			}
		}
		else {
			int is_add;
			uint32_t *addr;
			is_add = (high_instruction & 0x80) >> RELOCATE_SEVEN;
			r = low_instruction >> RELOCATE_TWELVE;
			imm32 = low_instruction & 0xFFF;
			if (is_add) {
				addr = (uint32_t *) (ALIGN_PC(pc) + imm32);
			}
			else {
				addr = (uint32_t *) (ALIGN_PC(pc) - imm32);
			}
			value = addr[0];
		}
		// LDR.W Rr, [PC, 2]
		trampoline_instructions[0] = 0xF8DF;
		trampoline_instructions[1] = r << RELOCATE_TWELVE | RELOCATE_FOUR;
		// nop
		trampoline_instructions[2] = 0xBF00;
		// B PC, #2
		trampoline_instructions[RELOCATE_THREE] = 0xE001;
		trampoline_instructions[RELOCATE_FOUR] = value & 0xFFFF;
		trampoline_instructions[RELOCATE_FIVE] = value >> RELOCATE_SIXTEEN;
		offset = RELOCATE_SIX;
	}
	else if (type == TBB_THUMB32 || type == TBH_THUMB32) {
		int rm;
		int r;
		int rx;
		rm = low_instruction & 0xF;
		for (r = RELOCATE_SEVEN;; --r) {
			if (r != rm) {
				break;
			}
		}
		for (rx = RELOCATE_SEVEN; ; --rx) {
			if (rx != rm && rx != r) {
				break;
			}
		}
		// PUSH {Rx}
		trampoline_instructions[0] = 0xB400 | (1 << rx);
		// LDR Rr, [PC, #20]
		trampoline_instructions[1] = 0x4805 | (r << RELOCATE_EIGHT);
		// MOV Rx, Rm
		trampoline_instructions[2] = 0x4600 | (rm << RELOCATE_THREE) | rx;
		if (type == TBB_THUMB32) {
			trampoline_instructions[RELOCATE_THREE] = 0xEB00 | r;
			// ADD.W Rx, Rr, Rx
			trampoline_instructions[RELOCATE_FOUR] = 0x0000 | (rx << RELOCATE_EIGHT) | rx;
			// LDRB Rx, [Rx]
			trampoline_instructions[RELOCATE_FIVE] = 0x7800 | (rx << RELOCATE_THREE) | rx;
		}
		else if (type == TBH_THUMB32) {
			trampoline_instructions[RELOCATE_THREE] = 0xEB00 | r;
			// ADD.W Rx, Rr, Rx, LSL #1
			trampoline_instructions[RELOCATE_FOUR] = 0x0040 | (rx << RELOCATE_EIGHT) | rx;
			// LDRH Rx, [Rx]
			trampoline_instructions[RELOCATE_FIVE] = 0x8800 | (rx << RELOCATE_THREE) | rx;
		}
		trampoline_instructions[RELOCATE_SIX] = 0xEB00 | r;
		// ADD Rr, Rr, Rx, LSL #1
		trampoline_instructions[RELOCATE_SEVEN] = 0x0040 | (r << RELOCATE_EIGHT) | rx;
		// ADD Rr, #1
		trampoline_instructions[8] = 0x3001 | (r << RELOCATE_EIGHT);
		// POP {Rx}
		trampoline_instructions[9] = 0xBC00 | (1 << rx);
		// BX Rr
		trampoline_instructions[RELOCATE_TEN] = 0x4700 | (r << RELOCATE_THREE);
		trampoline_instructions[RELOCATE_ELEVEN] = 0xBF00;
		trampoline_instructions[RELOCATE_TWELVE] = pc & 0xFFFF;
		trampoline_instructions[RELOCATE_THIRTEEN] = pc >> RELOCATE_SIXTEEN;
		offset = RELOCATE_FOURTEEN;
	}
	else {
		trampoline_instructions[0] = high_instruction;
		trampoline_instructions[1] = low_instruction;
		offset = RELOCATE_TWO;
	}
	return offset;
}
/**
 * 对Thumb指令进行修正
 *
 * @param target_addr  待Hook的目标函数地址，即当前 PC 值，用于修正指令
 * @param orig_instructions 存放原有指令的首地址，用于修正指令和后续对原有指令的恢复
 * @param length 存放的原有指令的长度，Arm 指令为8字节；Thumb 指令为12字节
 * @param trampoline_instructions  存放修正后指令的首地址，用于调用原函数
 * @param orig_boundaries 存放原有指令的指令边界（所谓边界即为该条指令与起始地址的偏移量），用于后续线程处理中，对 PC 的迁移
 * @param trampoline_boundaries 存放修正后指令的指令边界
 * @param count 处理的指令项数
 */
static void relocateInstructionInThumb(uint32_t target_addr, uint16_t *orig_instructions, int length, uint16_t *trampoline_instructions, int *orig_boundaries, int *trampoline_boundaries, int *count) {
	int orig_pos;
	int trampoline_pos;
	uint32_t pc;
	uint32_t lr;
	orig_pos = 0;
	trampoline_pos = 0;
	pc = target_addr + RELOCATE_FOUR;
	while (1) {
		int offset;
		orig_boundaries[*count] = orig_pos * sizeof(uint16_t);
		trampoline_boundaries[*count] = trampoline_pos * sizeof(uint16_t);
		++(*count);
		if ((orig_instructions[orig_pos] >> RELOCATE_ELEVEN) >= 0x1D && (orig_instructions[orig_pos] >> RELOCATE_ELEVEN) <= 0x1F) {
			if (orig_pos + RELOCATE_TWO > length / sizeof(uint16_t)) {
				break;
			}
			offset = relocateInstructionInThumb32(pc, orig_instructions[orig_pos], orig_instructions[orig_pos + 1], &trampoline_instructions[trampoline_pos]);
			pc += sizeof(uint32_t);
			trampoline_pos += offset;
			orig_pos += RELOCATE_TWO;
		}
		else {
			offset = relocateInstructionInThumb16(pc, orig_instructions[orig_pos], &trampoline_instructions[trampoline_pos]);
			pc += sizeof(uint16_t);
			trampoline_pos += offset;
			++orig_pos;
		}
		if (orig_pos >= length / sizeof(uint16_t)) {
			break;
		}
	}
	lr = target_addr + orig_pos * sizeof(uint16_t) + 1;
	trampoline_instructions[trampoline_pos] = 0xF8DF;
	trampoline_instructions[trampoline_pos + 1] = 0xF000;	// LDR.W PC, [PC]
	trampoline_instructions[trampoline_pos + RELOCATE_TWO] = lr & 0xFFFF;
	trampoline_instructions[trampoline_pos + RELOCATE_THREE] = lr >> RELOCATE_SIXTEEN;
}
/**
 * 对ARM指令进行修正
 *
 * @param target_addr  待Hook的目标函数地址，即当前 PC 值，用于修正指令
 * @param orig_instructions 存放原有指令的首地址，用于修正指令和后续对原有指令的恢复
 * @param length 存放的原有指令的长度，Arm 指令为8字节；Thumb 指令为12字节
 * @param trampoline_instructions  存放修正后指令的首地址，用于调用原函数
 * @param orig_boundaries 存放原有指令的指令边界（所谓边界即为该条指令与起始地址的偏移量），用于后续线程处理中，对 PC 的迁移
 * @param trampoline_boundaries 存放修正后指令的指令边界
 * @param count 处理的指令项数
 */
static void relocateInstructionInArm(uint32_t target_addr, uint32_t *orig_instructions, int length, uint32_t *trampoline_instructions, int *orig_boundaries, int *trampoline_boundaries, int *count) {
	uint32_t pc;
	uint32_t lr;
	int orig_pos;
	int trampoline_pos;
	pc = target_addr + RELOCATE_EIGHT;
	lr = target_addr + length;
	trampoline_pos = 0;
	for (orig_pos = 0; orig_pos < length / sizeof(uint32_t); ++orig_pos) {
		uint32_t instruction;
		int type;
		orig_boundaries[*count] = orig_pos * sizeof(uint32_t);
		trampoline_boundaries[*count] = trampoline_pos * sizeof(uint32_t);
		++(*count);
		instruction = orig_instructions[orig_pos];
		type = getTypeInArm(instruction);
		// BLX_ARM、BL_ARM、B_ARM、BX_ARM指令的修正
		if (type == BLX_ARM || type == BL_ARM || type == B_ARM || type == BX_ARM) {
			uint32_t x;
			int top_bit;
			uint32_t imm32;
			uint32_t value;
			if (type == BLX_ARM || type == BL_ARM) {
				// ADD LR, PC, #4
				trampoline_instructions[trampoline_pos++] = 0xE28FE004;
			}
			// LDR PC, [PC, #-4]，构造跳转指令
			trampoline_instructions[trampoline_pos++] = 0xE51FF004;
			if (type == BLX_ARM) {
				x = ((instruction & 0xFFFFFF) << RELOCATE_TWO) | ((instruction & 0x1000000) >> RELOCATE_TWENTY_THREE);
			}
			else if (type == BL_ARM || type == B_ARM) {
				x = (instruction & 0xFFFFFF) << RELOCATE_TWO;
			}
			else {
				x = 0;
			}
			top_bit = x >> RELOCATE_TWENTY_FIVE;
			imm32 = top_bit ? (x | (0xFFFFFFFF << RELOCATE_TWENTY_SIX)) : x;
			if (type == BLX_ARM) {
				value = pc + imm32 + 1;
			}
			else {
				value = pc + imm32;
			}
			trampoline_instructions[trampoline_pos++] = value;
		}
		else if (type == ADD_ARM) {
			int rd;
			int rm;
			int r;
			// 解析指令得到rd、rm寄存器
			rd = (instruction & 0xF000) >> RELOCATE_TWELVE;
			rm = instruction & 0xF;
			// 为避免冲突，排除rd、rm寄存器，选择一个临时寄存器Rr
			for (r = RELOCATE_TWELVE; ; --r) {
				if (r != rd && r != rm) {
					break;
				}
			}
			// PUSH {Rr}，保护Rr寄存器值
			trampoline_instructions[trampoline_pos++] = 0xE52D0004 | (r << RELOCATE_TWELVE);
			// LDR Rr, [PC, #8]，将PC值存入Rr寄存器中
			trampoline_instructions[trampoline_pos++] = 0xE59F0008 | (r << RELOCATE_TWELVE);
			// 变换原指令
			trampoline_instructions[trampoline_pos++] = (instruction & 0xFFF0FFFF) | (r << RELOCATE_SIXTEEN);
			// POP {Rr}，恢复Rr寄存器值
			trampoline_instructions[trampoline_pos++] = 0xE49D0004 | (r << RELOCATE_TWELVE);
			// ADD PC, PC，跳过下一条指令
			trampoline_instructions[trampoline_pos++] = 0xE28FF000;
			trampoline_instructions[trampoline_pos++] = pc;
		}
		else if (type == ADR1_ARM || type == ADR2_ARM || type == LDR_ARM || type == MOV_ARM) {
			int r;
			uint32_t value;
			r = (instruction & 0xF000) >> RELOCATE_TWELVE;
			if (type == ADR1_ARM || type == ADR2_ARM || type == LDR_ARM) {
				uint32_t imm32;
				imm32 = instruction & 0xFFF;
				if (type == ADR1_ARM) {
					value = pc + imm32;
				}
				else if (type == ADR2_ARM) {
					value = pc - imm32;
				}
				else if (type == LDR_ARM) {
					int is_add;
					is_add = (instruction & 0x800000) >> RELOCATE_TWENTY_THREE;
					if (is_add) {
						value = ((uint32_t *) (pc + imm32))[0];
					}
					else {
						value = ((uint32_t *) (pc - imm32))[0];
					}
				}
			}
			else {
				value = pc;
			}
			// LDR Rr, [PC]
			trampoline_instructions[trampoline_pos++] = 0xE51F0000 | (r << RELOCATE_TWELVE);
			// ADD PC, PC，跳过下一条指令
			trampoline_instructions[trampoline_pos++] = 0xE28FF000;
			trampoline_instructions[trampoline_pos++] = value;
		}
		else {
			// 直接将指令存放到trampoline_instructions中
			trampoline_instructions[trampoline_pos++] = instruction;
		}
		pc += sizeof(uint32_t);
	}
	// LDR PC, [PC, #-4]
	trampoline_instructions[trampoline_pos++] = 0xe51ff004;
	trampoline_instructions[trampoline_pos++] = lr;
}
/**
 * 将原始函数的被跳转指令替换的那几个指令拷贝到 trampoline_instructions 中
 * 此时 PC 值已经变动，所以还需要对相关指令进行修正
 *
 * @param target_addr  待Hook的目标函数地址，即当前 PC 值，用于修正指令
 * @param orig_instructions 存放原有指令的首地址，用于修正指令和后续对原有指令的恢复
 * @param length 存放的原有指令的长度，Arm 指令为8字节；Thumb 指令为12字节
 * @param trampoline_instructions  存放修正后指令的首地址，用于调用原函数
 * @param orig_boundaries 存放原有指令的指令边界（所谓边界即为该条指令与起始地址的偏移量），用于后续线程处理中，对 PC 的迁移
 * @param trampoline_boundaries 存放修正后指令的指令边界
 * @param count 处理的指令项数
 */
void relocateInstruction(uint32_t target_addr, void *orig_instructions, int length, void *trampoline_instructions, int *orig_boundaries, int *trampoline_boundaries, int *count) {
	if (target_addr & 1 == 1) {
		relocateInstructionInThumb(target_addr - 1, (uint16_t *) orig_instructions, length, (uint16_t *) trampoline_instructions, orig_boundaries, trampoline_boundaries, count);
	}
	else {
		relocateInstructionInArm(target_addr, (uint32_t *) orig_instructions, length, (uint32_t *) trampoline_instructions, orig_boundaries, trampoline_boundaries, count);
	}
}
}