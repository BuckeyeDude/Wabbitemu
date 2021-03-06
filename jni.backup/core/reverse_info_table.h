#ifndef REVERSE_INFO_TABLE_H
#define REVERSE_INFO_TABLE_H

#ifdef WITH_REVERSE
#include "reverse_info.h"

/* This will all compress quite well, through UPX */

// Reverse opcode table
static opcodep opcode_reverse_info[256] = {
	&nop_reverse_info,				//0
	&ld_bc_num16_reverse_info,
	&ld_bc_a_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&rlca_reverse_info,
	
	&nop_reverse_info,
	&add_hl_reg16_reverse_info,
	&ld_a_bc_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&rrca_reverse_info,
	
	&djnz_reverse_info,				//10
	&ld_de_num16_reverse_info,
	&ld_de_a_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&rla_reverse_info,
	
	&jr_reverse_info,
	&add_hl_reg16_reverse_info,
	&ld_a_de_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&rra_reverse_info,
	
	&jr_condition_reverse_info,		//20
	&ld_hl_num16_reverse_info,
	&ld_mem16_hlf_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&daa_reverse_info,
	
	&jr_condition_reverse_info,
	&add_hl_reg16_reverse_info,
	&ld_hlf_mem16_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&cpl_reverse_info,
	
	&jr_condition_reverse_info,		//30
	&ld_sp_num16_reverse_info,
	&ld_mem16_a_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&nop_reverse_info,
	
	&jr_condition_reverse_info,
	&add_hl_reg16_reverse_info,
	&ld_a_mem16_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&ld_r_num8_reverse_info,
	&nop_reverse_info,
	
	&ld_r_r_reverse_info,		//40
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,		//50
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,		//60
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,		//70
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&halt_reverse_info,
	&ld_r_r_reverse_info,
	
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	&ld_r_r_reverse_info,
	
	&add_a_reg8_reverse_info,	//80
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	&add_a_reg8_reverse_info,
	
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	&adc_a_reg8_reverse_info,
	
	&sub_a_reg8_reverse_info,		//90
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	&sub_a_reg8_reverse_info,
	
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	&sbc_a_reg8_reverse_info,
	
	&and_reg8_reverse_info,		//a0
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	&and_reg8_reverse_info,
	
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	&xor_reg8_reverse_info,
	
	&or_reg8_reverse_info,		//b0
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	&or_reg8_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&ret_condition_reverse_info,		//c0
	&pop_reg16_reverse_info,
	&jp_condition_reverse_info,
	&jp_reverse_info,
	&call_condition_reverse_info,
	&push_reg16_reverse_info,
	&add_a_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,
	&ret_reverse_info,
	&jp_condition_reverse_info,
	&CPU_CB_opcode_run_reverse_info,
	&call_condition_reverse_info,
	&call_reverse_info,
	&adc_a_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,		//d0
	&pop_reg16_reverse_info,
	&jp_condition_reverse_info,
	&nop_reverse_info,
	&call_condition_reverse_info,
	&push_reg16_reverse_info,
	&sub_a_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,
	&nop_reverse_info,
	&jp_condition_reverse_info,
	&in_reverse_info,
	&call_condition_reverse_info,
	&nop_reverse_info,				//DD prefix
	&sbc_a_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,		//e0
	&pop_reg16_reverse_info,
	&jp_condition_reverse_info,
	&nop_reverse_info,
	&call_condition_reverse_info,
	&push_reg16_reverse_info,
	&and_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,
	&jp_hl_reverse_info,
	&jp_condition_reverse_info,
	&nop_reverse_info,
	&call_condition_reverse_info,
	&CPU_ED_opcode_run_reverse_info,
	&xor_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,		//f0
	&pop_reg16_reverse_info,
	&jp_condition_reverse_info,
	&di_reverse_info,
	&call_condition_reverse_info,
	&push_reg16_reverse_info,
	&or_num8_reverse_info,
	&rst_reverse_info,
	
	&ret_condition_reverse_info,
	&ld_sp_hl_reverse_info,
	&jp_condition_reverse_info,
	&ei_reverse_info,
	&call_condition_reverse_info,
	&nop_reverse_info,				//FD prefix
	&nop_reverse_info,
	&rst_reverse_info
};

//CB opcodes
static opcodep CBtab_reverse_info[256] = {
	&rlc_reg_reverse_info,			//00
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,
	&rlc_reg_reverse_info,

	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,
	&rrc_reg_reverse_info,

	&rl_reg_reverse_info,			//10
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	&rl_reg_reverse_info,
	
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	&rr_reg_reverse_info,
	
	&sla_reg_reverse_info,		//20
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	&sla_reg_reverse_info,
	
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	&sra_reg_reverse_info,
	
	&sll_reg_reverse_info,		//30
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	&sll_reg_reverse_info,
	
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	&srl_reg_reverse_info,
	
	&nop_reverse_info,			//40
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,			//50
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,			//60
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,			//70
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&res_reverse_info,			//80
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,			//90
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,			//a0
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,			//b0
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,
	&res_reverse_info,

	&set_reverse_info,			//c0
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,			//d0
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,			//e0
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,			//f0
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,

	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info,
	&set_reverse_info
};

// index register cb opcodes
static index_opcodep ICB_opcode_reverse_info[256] = {
	&rlc_ind_reverse_info,			//00
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,
	&rlc_ind_reverse_info,

	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,
	&rrc_ind_reverse_info,

	&rl_ind_reverse_info,			//10
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	&rl_ind_reverse_info,
	
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	&rr_ind_reverse_info,
	
	&sla_ind_reverse_info,		//20
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	&sla_ind_reverse_info,
	
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	&sra_ind_reverse_info,
	
	&sll_ind_reverse_info,		//30
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	&sll_ind_reverse_info,
	
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	&srl_ind_reverse_info,
	
	&nop_ind_reverse_info,			//40
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,			//50
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,			//60
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,			//70
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,
	&nop_ind_reverse_info,

	&res_ind_reverse_info,			//80
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,			//90
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,			//a0
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,			//b0
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,
	&res_ind_reverse_info,

	&set_ind_reverse_info,			//c0
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,			//d0
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,			//e0
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,			//f0
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,

	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info,
	&set_ind_reverse_info
};

//ED opcodes
/*  According Sean McLaughlin's 28day's 
	the majority of ED prefixed opcodes 
	are nops...
	*/
static opcodep EDtab_reverse_info[256] = {
	&nop_reverse_info,				//00
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//10
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//20
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//30
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,			//40
	&nop_reverse_info,
	&sbc_hl_reg16_reverse_info,
	&ld_mem16_reg16_reverse_info,
	&nop_reverse_info,
	&retn_reverse_info,
	&IM0_reverse_info,
	&ld_i_a_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&adc_hl_reg16_reverse_info,
	&ld_reg16_mem16_reverse_info,
	&nop_reverse_info,
	&reti_reverse_info,
	&IM0_reverse_info,
	&ld_r_a_reverse_info,
	
	&nop_reverse_info,			//50
	&nop_reverse_info,
	&sbc_hl_reg16_reverse_info,
	&ld_mem16_reg16_reverse_info,
	&nop_reverse_info,
	&retn_reverse_info,
	&IM1_reverse_info,
	&ld_a_i_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&adc_hl_reg16_reverse_info,
	&ld_reg16_mem16_reverse_info,
	&nop_reverse_info,
	&reti_reverse_info,
	&IM2_reverse_info,
	&ld_a_r_reverse_info,
	
	&nop_reverse_info,			//60
	&nop_reverse_info,
	&sbc_hl_reg16_reverse_info,
	&ld_mem16_reg16_reverse_info,
	&nop_reverse_info,
	&retn_reverse_info,
	&IM0_reverse_info,
	&rrd_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&adc_hl_reg16_reverse_info,
	&ld_reg16_mem16_reverse_info,
	&nop_reverse_info,
	&reti_reverse_info,
	&IM0_reverse_info,
	&rld_reverse_info,
	
	&nop_reverse_info,			//70
	&nop_reverse_info,
	&sbc_hl_reg16_reverse_info,
	&ld_mem16_reg16_reverse_info,
	&nop_reverse_info,
	&retn_reverse_info,
	&IM1_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&adc_hl_reg16_reverse_info,
	&ld_reg16_mem16_reverse_info,
	&nop_reverse_info,
	&reti_reverse_info,
	&IM2_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//80
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//90
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
		

	&ldi_reverse_info,				//a0
	&nop_reverse_info,
	&ini_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&ldd_reverse_info,
	&nop_reverse_info,
	&ind_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&ldir_reverse_info,				//b0
	&nop_reverse_info,
	&inir_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&lddr_reverse_info,
	&nop_reverse_info,
	&indr_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,				//c0
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//d0
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//e0
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,

	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,				//f0
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info,
	&nop_reverse_info
};

#endif
#endif