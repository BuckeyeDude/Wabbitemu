#include "stdafx.h"

#include "colorlcd.h"

#define PIXEL_OFFSET(x, y) ((y) * COLOR_LCD_WIDTH + (x)) * COLOR_LCD_DEPTH 
#define TRUCOLOR(color, bits) ((color) * (0xFF / ((1 << (bits)) - 1)))
#define LCD_REG(reg) (lcd->registers[reg])
#define LCD_REG_MASK(reg, mask) (LCD_REG(reg) & (mask))

typedef enum {
	DRIVER_CODE_REG = 0x00, 
	DRIVER_OUTPUT_CONTROL1_REG = 0x01,				DRIVER_OUTPUT_CONTROL1_MASK = 0x0500,
	ENTRY_MODE_REG = 0x03,							ENTRY_MODE_MASK = 0xD0B8,
	DATA_FORMAT_16BIT_REG = 0x05,					DATA_FORMAT_16BIT_MASK = 0x0003,
	DISPLAY_CONTROL1_REG = 0x07,					DISPLAY_CONTROL1_MASK= 0x313B,
	DISPLAY_CONTROL2_REG = 0x08,					DISPLAY_CONTROL2_MASK = 0xFFFF,
	DISPLAY_CONTROL3_REG = 0x09,					DISPLAY_CONTROL3_MASK = 0x073F,
	DISPLAY_CONTROL4_REG = 0x0A,					DISPLAY_CONTROL4_MASK = 0x000F,
	RGB_DISPLAY_INTERFACE_CONTROL1_REG = 0x0C,		RGB_DISPLAY_INTERFACE_CONTROL1_MASK = 0x7133,
	FRAME_MARKER_REG = 0x0D,						FRAME_MARKER_MASK = 0x01FF,
	RGB_DISPLAY_INTERFACE_CONTROL2_REG = 0x0F,		RGB_DISPLAY_INTERFACE_CONTROL2_MASK = 0x001B,
	POWER_CONTROL1_REG = 0x10,						POWER_CONTROL1_MASK = 0x17F3,
	POWER_CONTROL2_REG = 0x11,						POWER_CONTROL2_MASK = 0x0777,
	POWER_CONTROL3_REG = 0x12,						POWER_CONTROL3_MASK = 0x008F,
	POWER_CONTROL4_REG = 0x13,						POWER_CONTROL4_MASK = 0x1F00,
	CUR_Y_REG = 0x20,								CUR_Y_MASK = 0x00FF,
	CUR_X_REG = 0x21,								CUR_X_MASK = 0x01FF,
	GRAM_REG = 0x22,
	POWER_CONTROL7_REG = 0x29,						POWER_CONTROL7_MASK = 0x003F,
	FRAME_RATE_COLOR_CONTROL_REG = 0x2B,			FRAME_RATE_COLOR_CONTROL_MASK = 0x000F,
	GAMMA_CONTROL1_REG = 0x30,						GAMMA_CONTROL1_MASK = 0x0707,
	GAMMA_CONTROL2_REG = 0x31,						GAMMA_CONTROL2_MASK = 0x0707,
	GAMMA_CONTROL3_REG = 0x32,						GAMMA_CONTROL3_MASK = 0x0707,
	GAMMA_CONTROL4_REG = 0x35,						GAMMA_CONTROL4_MASK = 0x0707,
	GAMMA_CONTROL5_REG = 0x36,						GAMMA_CONTROL5_MASK = 0x1F0F,
	GAMMA_CONTROL6_REG = 0x37,						GAMMA_CONTROL6_MASK = 0x0707,
	GAMMA_CONTROL7_REG = 0x38,						GAMMA_CONTROL7_MASK = 0x0707,
	GAMMA_CONTROL8_REG = 0x39,						GAMMA_CONTROL8_MASK = 0x0707,
	GAMMA_CONTROL9_REG = 0x3C,						GAMMA_CONTROL9_MASK = 0x0707,
	GAMMA_CONTROL10_REG = 0x3D,						GAMMA_CONTROL10_MASK = 0x1F0F,
	WINDOW_HORZ_START_REG = 0x50,					WINDOW_HORZ_START_MASK = 0x00FF,
	WINDOW_HORZ_END_REG = 0x51,						WINDOW_HORZ_END_MASK = 0x00FF,
	WINDOW_VERT_START_REG = 0x52,					WINDOW_VERT_START_MASK = 0x01FF,
	WINDOW_VERT_END_REG = 0x53,						WINDOW_VERT_END_MASK = 0x01FF,
	GATE_SCAN_CONTROL_REG = 0x60,					GATE_SCAN_CONTROL_MASK = 0xBF3F,
	BASE_IMAGE_DISPLAY_CONTROL_REG = 0x61,			BASE_IMAGE_DISPLAY_CONTROL_MASK = 0x0007,
	VERTICAL_SCROLL_CONTROL_REG = 0x6A,				VERTICAL_SCROLL_CONTROL_MASK = 0x01FF,
	PARTIAL_IMAGE1_DISPLAY_POSITION_REG = 0x80,		PARTIAL_IMAGE1_DISPLAY_POSITION_MASK = 0x01FF,
	PARTIAL_IMAGE1_START_LINE_REG = 0x81,			PARTIAL_IMAGE1_START_LINE_MASK = 0x01FF,
	PARTIAL_IMAGE1_END_LINE_REG = 0x82,				PARTIAL_IMAGE1_END_LINE_MASK = 0x01FF,
	PARTIAL_IMAGE2_DISPLAY_POSITION_REG = 0x83,		PARTIAL_IMAGE2_DISPLAY_POSITION_MASK = 0x01FF,
	PARTIAL_IMAGE2_START_LINE_REG = 0x84,			PARTIAL_IMAGE2_START_LINE_MASK = 0x01FF,
	PARTIAL_IMAGE2_END_LINE_REG = 0x85,				PARTIAL_IMAGE2_END_LINE_MASK = 0x01FF,
	PANEL_INTERFACE_CONTROL1_REG = 0x90,			PANEL_INTERFACE_CONTROL1_MASK = 0x031F,
	PANEL_INTERFACE_CONTROL2_REG = 0x92,			PANEL_INTERFACE_CONTROL2_MASK = 0x0700,
	PANEL_INTERFACE_CONTROL4_REG = 0x95,			PANEL_INTERFACE_CONTROL4_MASK = 0x0300,
	PANEL_INTERFACE_CONTROL5_REG = 0x97,			PANEL_INTERFACE_CONTROL5_MASK = 0x0F00,
	OTP_VCM_PROGRAMMING_CONTROL_REG = 0xA1,			OTP_VCM_PROGRAMMING_CONTROL_MASK = 0x083F,
	OTP_VCM_STATUS_AND_ENABLE_REG = 0xA2,			OTP_VCM_STATUS_AND_ENABLE_MASK = 0xFF01,
	OTP_PROGRAMMING_ID_KEY_REG = 0xA5,				OTP_PROGRAMMING_ID_KEY_MASK = 0xFFFF,
	DEEP_STAND_BY_MODE_CONTROL_REG = 0xE6,			DEEP_STAND_BY_MODE_CONTROL_MASK = 0x0001
} COLOR_LCD_COMMAND;

// 0
#define DRIVER_CODE_VER 0x9335

// 1
#define FLIP_COLS_MASK BIT(8)
#define INTERLACED_MASK BIT(10)

// 3
#define CUR_DIR_MASK BIT(3)
#define ROW_INC_MASK BIT(4)
#define COL_INC_MASK BIT(5)
#define ORG_MASK BIT(7)
#define BGR_MASK BIT(12)
#define EIGHTEEN_BIT_MASK BIT(14)
#define UNPACKED_MASK BIT(15)
#define TRI_MASK EIGHTEEN_BIT_MASK | UNPACKED_MASK

// 7
#define DISPLAY_ON_MASK (BIT(0) | BIT(1))
#define COLOR8_MASK BIT(3)
#define BASEE_MASK BIT(8)
#define SHOW_PARTIAL1_MASK BIT(12)
#define SHOW_PARTIAL2_MASK BIT(13)

// 2B
#define FRAME_RATE_MASK (BIT(3) | BIT(2) | BIT(1) | BIT(0))

// 60
#define BASE_START_MASK (BIT(0) | BIT(1) | BIT(2) | BIT(3) | BIT(4) | BIT(5))
#define BASE_NLINES_MASK (BIT(8) | BIT(9) | BIT(10) | BIT(11) | BIT(12) | BIT(13))
#define GATE_SCAN_DIR_MASK BIT(15)

// 61
#define LEVEL_INVERT_MASK BIT(0)
#define SCROLL_ENABLED_MASK BIT(1)
#define NDL_MASK BIT(2)

// 6a
#define SCROLL_MASK 0x1FF

// 80
#define P1_POS_MASK 0x1FF

// 81
#define P1_START_MASK 0x1FF

// 82
#define P1_END_MASK 0x1FF

// 83
#define P2_POS_MASK 0x1FF

// 84
#define P2_START_MASK 0x1FF

// 85
#define P2_END_MASK 0x1FF

static int read_pixel(ColorLCD_t *lcd);
static void write_pixel18(ColorLCD_t *lcd);
static void write_pixel16(ColorLCD_t *lcd);
static void update_x(ColorLCD_t *lcd, BOOL should_update_row);
static void update_y(ColorLCD_t *lcd, BOOL should_update_col);

static void ColorLCD_enqueue(CPU_t *, ColorLCD_t *);
static void ColorLCD_reset(CPU_t *);
static void ColorLCD_LCDreset(ColorLCD_t *lcd);
static void ColorLCD_free(CPU_t *);
static void ColorLCD_command(CPU_t *, device_t *);
static void ColorLCD_data(CPU_t *, device_t *);
uint8_t *ColorLCD_Image(LCDBase_t *);

ColorLCD_t *ColorLCD_init(CPU_t *cpu, int model) {
	ColorLCD_t* lcd = (ColorLCD_t *)malloc(sizeof(ColorLCD_t));
	if (lcd == NULL) {
		printf("Couldn't allocate memory for LCD\n");
		exit(1);
	}
	
	ColorLCD_LCDreset(lcd);
	return lcd;
}

void set_line_time(ColorLCD_t *lcd) {
	// FrameFrequency * (DisplayLine + FrontPorch + BackPorch) * ClockCyclePerLines
	uint64_t refresh_time = lcd->frame_rate * (lcd->display_lines + lcd->front_porch + lcd->back_porch) * lcd->clocks_per_line;
	refresh_time /= lcd->clock_divider;
	//refresh_time /= COLOR_LCD_HEIGHT;
	lcd->line_time = 1.0 / refresh_time;
}


static void reset_y(ColorLCD_t *lcd, uint16_t mode) {
	if (mode & ROW_INC_MASK) {
		lcd->base.y = lcd->registers[WINDOW_HORZ_START_REG];
	} else {
		lcd->base.y = lcd->registers[WINDOW_HORZ_END_REG];
	}
}

static void reset_x(ColorLCD_t *lcd, uint16_t mode) {
	if (mode & COL_INC_MASK) {
		lcd->base.x = lcd->registers[WINDOW_VERT_START_REG];
	} else {
		lcd->base.x = lcd->registers[WINDOW_VERT_END_REG];
	}
}

void ColorLCD_set_register(CPU_t *cpu, ColorLCD_t *lcd, uint16_t reg, uint16_t value) {
	uint16_t mode = LCD_REG(ENTRY_MODE_REG);

	switch (reg) {
	case DRIVER_CODE_REG:
		break;
	case DRIVER_OUTPUT_CONTROL1_REG:
		lcd->registers[DRIVER_OUTPUT_CONTROL1_REG] = value & DRIVER_OUTPUT_CONTROL1_MASK;
		break;
	case ENTRY_MODE_REG: {
		lcd->registers[ENTRY_MODE_REG] = value & ENTRY_MODE_MASK;
		if (mode & ORG_MASK) {
			reset_x(lcd, value);
			reset_y(lcd, value);
		}
		break;
	}
	case DATA_FORMAT_16BIT_REG:
		lcd->registers[DATA_FORMAT_16BIT_REG] = value & DATA_FORMAT_16BIT_MASK;
		break;
	case DISPLAY_CONTROL1_REG:
		lcd->registers[DISPLAY_CONTROL1_REG] = value & DISPLAY_CONTROL1_MASK;
		lcd->base.active = value & DISPLAY_ON_MASK ? TRUE : FALSE;
		// if active changed, we need to notify the change immediately
		ColorLCD_enqueue(cpu, lcd);
		break;
	case DISPLAY_CONTROL2_REG:
		lcd->registers[DISPLAY_CONTROL2_REG] = value & DISPLAY_CONTROL2_MASK;
		lcd->back_porch = value & 0xFF;
		lcd->front_porch = value >> 8;
		set_line_time(lcd);
		break;
	case DISPLAY_CONTROL3_REG:
		lcd->registers[DISPLAY_CONTROL3_REG] = value & DISPLAY_CONTROL3_MASK;
		break;
	case DISPLAY_CONTROL4_REG:
		lcd->registers[DISPLAY_CONTROL4_REG] = value & DISPLAY_CONTROL4_MASK;
		break;
	case RGB_DISPLAY_INTERFACE_CONTROL1_REG:
		lcd->registers[RGB_DISPLAY_INTERFACE_CONTROL1_REG] = value & RGB_DISPLAY_INTERFACE_CONTROL1_MASK;
		break;
	case FRAME_MARKER_REG:
		lcd->registers[FRAME_MARKER_REG] = value & FRAME_MARKER_MASK;
		break;
	case RGB_DISPLAY_INTERFACE_CONTROL2_REG:
		lcd->registers[RGB_DISPLAY_INTERFACE_CONTROL2_REG] = value & RGB_DISPLAY_INTERFACE_CONTROL2_MASK;
		break;
	case POWER_CONTROL1_REG:
		lcd->registers[POWER_CONTROL1_REG] = value & POWER_CONTROL1_MASK;
		break;
	case POWER_CONTROL2_REG:
		lcd->registers[POWER_CONTROL2_REG] = value & POWER_CONTROL2_MASK;
		break;
	case POWER_CONTROL3_REG:
		lcd->registers[POWER_CONTROL3_REG] = value & POWER_CONTROL3_MASK;
		break;
	case POWER_CONTROL4_REG:
		lcd->registers[POWER_CONTROL4_REG] = value & POWER_CONTROL4_MASK;
		break;
	case CUR_X_REG:
	case CUR_Y_REG: {
		lcd->registers[reg] = value & (reg == CUR_X_REG ? CUR_X_MASK : CUR_Y_MASK);

		if (mode & ORG_MASK) {
			if (reg == CUR_Y_REG) {
				reset_y(lcd, mode);
			} else {
				reset_x(lcd, mode);
			}
		} else {
			lcd->base.x = LCD_REG(CUR_X_REG);
			lcd->base.y = LCD_REG(CUR_Y_REG);
		}
		break;
	}
	case POWER_CONTROL7_REG:
		lcd->registers[POWER_CONTROL7_REG] = value & POWER_CONTROL7_MASK;
		break;
	case FRAME_RATE_COLOR_CONTROL_REG:
		lcd->registers[FRAME_RATE_COLOR_CONTROL_REG] = value & FRAME_RATE_COLOR_CONTROL_MASK;
		lcd->panic_mode = FALSE;
		switch (value & FRAME_RATE_MASK) {
		case 0:
			lcd->frame_rate = 31;
			break;
		case 1:
			lcd->frame_rate = 32;
			break;
		case 2:
			lcd->frame_rate = 34;
			break;
		case 3:
			lcd->frame_rate = 36;
			break;
		case 4:
			lcd->frame_rate = 39;
			break;
		case 5:
			lcd->frame_rate = 41;
			break;
		case 6:
			lcd->frame_rate = 34;
			break;
		case 7:
			lcd->frame_rate = 48;
			break;
		case 8:
			lcd->frame_rate = 52;
			break;
		case 9:
			lcd->frame_rate = 57;
			break;
		case 10:
			lcd->frame_rate = 62;
			break;
		case 11:
			lcd->frame_rate = 69;
			break;
		case 12:
			lcd->frame_rate = 78;
			break;
		case 13:
			lcd->frame_rate = 89;
			break;
		default:
			lcd->panic_mode = TRUE;
			break;
		}
		set_line_time(lcd);
		break;
	case GAMMA_CONTROL1_REG:
	case GAMMA_CONTROL2_REG:
	case GAMMA_CONTROL3_REG:
	case GAMMA_CONTROL4_REG:
	case GAMMA_CONTROL6_REG:
	case GAMMA_CONTROL7_REG:
	case GAMMA_CONTROL8_REG:
	case GAMMA_CONTROL9_REG:
		lcd->registers[reg] = value & GAMMA_CONTROL1_MASK;
		break;
	case GAMMA_CONTROL5_REG:
	case GAMMA_CONTROL10_REG:
		lcd->registers[reg] = value & GAMMA_CONTROL5_MASK;
		break;
	case WINDOW_HORZ_START_REG: {
		lcd->registers[WINDOW_HORZ_START_REG] = value & WINDOW_HORZ_START_MASK;
		if ((mode & ORG_MASK) && (mode & COL_INC_MASK)) {
			lcd->base.y = LCD_REG(WINDOW_HORZ_START_REG);
		}
		break;
	}
	case WINDOW_HORZ_END_REG: {
		lcd->registers[WINDOW_HORZ_END_REG] = value & WINDOW_HORZ_END_MASK;
		if ((mode & ORG_MASK) && !(mode & COL_INC_MASK)) {
			lcd->base.y = lcd->registers[WINDOW_HORZ_END_REG];
		}
		break;
	}
	case WINDOW_VERT_START_REG: {
		lcd->registers[WINDOW_VERT_START_REG] = value & WINDOW_VERT_START_MASK;
		if ((mode & ORG_MASK) && (mode & ROW_INC_MASK)) {
			lcd->base.x = LCD_REG(WINDOW_VERT_START_REG);
		}
		break;
	}
	case WINDOW_VERT_END_REG: {
		lcd->registers[WINDOW_VERT_END_REG] = value & WINDOW_VERT_END_MASK;
		if ((mode & ORG_MASK) && !(mode & ROW_INC_MASK)) {
			lcd->base.x = LCD_REG(WINDOW_VERT_END_REG);
		}
		break;
	}
	case GATE_SCAN_CONTROL_REG:
		lcd->registers[GATE_SCAN_CONTROL_REG] = value & GATE_SCAN_CONTROL_MASK;
		break;
	case BASE_IMAGE_DISPLAY_CONTROL_REG:
		lcd->registers[BASE_IMAGE_DISPLAY_CONTROL_REG] = value & BASE_IMAGE_DISPLAY_CONTROL_MASK;
		break;
	case VERTICAL_SCROLL_CONTROL_REG:
		lcd->base.z = 
			lcd->registers[VERTICAL_SCROLL_CONTROL_REG] = value & VERTICAL_SCROLL_CONTROL_MASK;
		break;
	case PARTIAL_IMAGE1_DISPLAY_POSITION_REG:
		lcd->registers[PARTIAL_IMAGE1_DISPLAY_POSITION_REG] = value & PARTIAL_IMAGE1_DISPLAY_POSITION_MASK;
		break;
	case PARTIAL_IMAGE1_START_LINE_REG:
		lcd->registers[PARTIAL_IMAGE1_START_LINE_REG] = value & PARTIAL_IMAGE1_START_LINE_MASK;
		break;
	case PARTIAL_IMAGE1_END_LINE_REG:
		lcd->registers[PARTIAL_IMAGE1_END_LINE_REG] = value & PARTIAL_IMAGE1_END_LINE_MASK;
		break;
	case PARTIAL_IMAGE2_DISPLAY_POSITION_REG:
		lcd->registers[PARTIAL_IMAGE2_DISPLAY_POSITION_REG] = value & PARTIAL_IMAGE2_DISPLAY_POSITION_MASK;
		break;
	case PARTIAL_IMAGE2_START_LINE_REG:
		lcd->registers[PARTIAL_IMAGE2_START_LINE_REG] = value & PARTIAL_IMAGE2_START_LINE_MASK;
		break;
	case PARTIAL_IMAGE2_END_LINE_REG:
		lcd->registers[PARTIAL_IMAGE2_END_LINE_REG] = value & PARTIAL_IMAGE2_END_LINE_MASK;
		break;
	case PANEL_INTERFACE_CONTROL1_REG:
		lcd->registers[PANEL_INTERFACE_CONTROL1_REG] = value & PANEL_INTERFACE_CONTROL1_MASK;
		lcd->clocks_per_line = value & 0xFF;
		switch (value >> 8) {
		case 0:
			lcd->clock_divider = 1;
			break;
		case 1:
			lcd->clock_divider = 2;
			break;
		case 2:
			lcd->clock_divider = 4;
			break;
		case 3:
			lcd->clock_divider = 8;
			break;
		}
		set_line_time(lcd);
		break;
	case PANEL_INTERFACE_CONTROL2_REG:
		lcd->registers[PANEL_INTERFACE_CONTROL2_REG] = value & PANEL_INTERFACE_CONTROL2_MASK;
		break;
	case PANEL_INTERFACE_CONTROL4_REG:
		lcd->registers[PANEL_INTERFACE_CONTROL4_REG] = value & PANEL_INTERFACE_CONTROL4_MASK;
		break;
	case PANEL_INTERFACE_CONTROL5_REG:
		lcd->registers[PANEL_INTERFACE_CONTROL5_REG] = value & PANEL_INTERFACE_CONTROL5_MASK;
		break;
	case OTP_VCM_PROGRAMMING_CONTROL_REG:
		lcd->registers[OTP_VCM_PROGRAMMING_CONTROL_REG] = value & OTP_VCM_PROGRAMMING_CONTROL_MASK;
		break;
	case OTP_VCM_STATUS_AND_ENABLE_REG:
		lcd->registers[OTP_VCM_STATUS_AND_ENABLE_REG] = value & OTP_VCM_STATUS_AND_ENABLE_MASK;
		break;
	case OTP_PROGRAMMING_ID_KEY_REG:
		lcd->registers[OTP_PROGRAMMING_ID_KEY_REG] = value & OTP_PROGRAMMING_ID_KEY_MASK;
		break;
	case DEEP_STAND_BY_MODE_CONTROL_REG:
		lcd->registers[DEEP_STAND_BY_MODE_CONTROL_REG] = value & DEEP_STAND_BY_MODE_CONTROL_MASK;
		break;
	default:
		lcd->registers[reg] = value & 0xFFFF;
		break;
	}
}

/*
* Free space belonging to lcd
*/
static void ColorLCD_free(CPU_t *cpu) {
	free(cpu->pio.lcd);
}
#define REAL_LCD2
#ifdef REAL_LCD
static void ColorLCD_enqueue(CPU_t *cpu, ColorLCD_t *lcd) {
	lcd->is_drawing = TRUE;
	// display sideways can't memcpy fuck
	if (lcd->draw_gate >= lcd->back_porch && lcd->draw_gate <= (COLOR_LCD_WIDTH + 1 - lcd->front_porch + lcd->back_porch)) {
		int offset = (lcd->draw_gate - lcd->back_porch) * COLOR_LCD_DEPTH;
		for (int i = 0; i < COLOR_LCD_HEIGHT; i++) {
			lcd->queued_image[offset] = lcd->display[offset];
			lcd->queued_image[offset + 1] = lcd->display[offset + 1];
			lcd->queued_image[offset + 2] = lcd->display[offset + 2];
			offset += COLOR_LCD_WIDTH * COLOR_LCD_DEPTH;
		}
	}

	lcd->last_draw = cpu->timer_c->elapsed;
	lcd->draw_gate++;
	if (lcd->draw_gate == COLOR_LCD_WIDTH + lcd->front_porch + lcd->back_porch) {
		lcd->draw_gate = 0;
		lcd->is_drawing = FALSE;
		if (cpu->lcd_enqueue_callback != NULL) {
			cpu->lcd_enqueue_callback(cpu);
		}
	}
}
#else
static void ColorLCD_enqueue(CPU_t *cpu, ColorLCD_t *lcd) {
	memcpy(lcd->queued_image, lcd->display, COLOR_LCD_DISPLAY_SIZE);

	if (cpu->lcd_enqueue_callback != NULL) {
		cpu->lcd_enqueue_callback(cpu);
	}
}
#endif

void ColorLCD_command(CPU_t *cpu, device_t *device) {
	ColorLCD_t *lcd = (ColorLCD_t *) device->aux;

	if (cpu->output) {
		// BIG ENDIAN
		lcd->current_register = cpu->bus | (lcd->current_register << 8);
		if (lcd->read_step) {
			lcd->read_step = 0;
		}

		if (lcd->write_step) {
			lcd->write_step = 0;
		}

		cpu->output = FALSE;
	} else if (cpu->input) {
		cpu->bus = 0;
		cpu->input = FALSE;
	}
}

void ColorLCD_data(CPU_t *cpu, device_t *device) {
	ColorLCD_t *lcd = (ColorLCD_t *)device->aux;
	uint16_t reg_index = lcd->current_register & 0xFF;
	if (cpu->output) {
		// Run some sanity checks on the write vars
		if (lcd->base.write_last > cpu->timer_c->elapsed)
			lcd->base.write_last = cpu->timer_c->elapsed;

		double write_delay = cpu->timer_c->elapsed - lcd->base.write_last;
		if (lcd->base.write_avg == 0.0) lcd->base.write_avg = write_delay;
		lcd->base.write_last = cpu->timer_c->elapsed;
		lcd->base.last_tstate = cpu->timer_c->tstates;

		// If there is a delay that is significantly longer than the
		// average write delay, we can assume a frame has just terminated
		// and you can push this complete frame towards generating the
		// final image.

		// If you are in steady mode, then this simply serves as a
		// FPS calculator
		if (write_delay < lcd->base.write_avg * 100.0) {
			lcd->base.write_avg = (lcd->base.write_avg * 0.90) + (write_delay * 0.10);
		} else {
			double ufps_length = cpu->timer_c->elapsed - lcd->base.ufps_last;
			lcd->base.ufps = 1.0 / ufps_length;
			lcd->base.ufps_last = cpu->timer_c->elapsed;
		}

		lcd->write_buffer = lcd->write_buffer << 8 | cpu->bus;
		if (reg_index == GRAM_REG) {
			int mode = LCD_REG_MASK(ENTRY_MODE_REG, TRI_MASK);
			if (mode & EIGHTEEN_BIT_MASK) {
				lcd->write_step++;
				if (lcd->write_step >= 3) {
					lcd->write_step = 0;
					write_pixel18(lcd);
				}
			} else {
				lcd->write_step = !lcd->write_step;
				if (!lcd->write_step) {
					write_pixel16(lcd);
				}
			}
		} else {
			lcd->write_step = !lcd->write_step;
			if (!lcd->write_step) {
				ColorLCD_set_register(cpu, lcd, reg_index, (uint16_t)lcd->write_buffer);
			}
		}

		BOOL isBreakpoint = lcd->register_breakpoint[reg_index];
		if (isBreakpoint) {
			cpu->pio.breakpoint_callback(cpu, lcd);
		}

		cpu->output = FALSE;
	} else if (cpu->input) {
		if (reg_index == GRAM_REG) {
			// read from LCD mem
			int pixel = lcd->read_buffer;
			cpu->bus = (unsigned char)(pixel >> 16);
			lcd->read_step = !lcd->read_step;
			if (!lcd->read_step) {
				pixel = read_pixel(lcd);
				pixel = ((pixel & 0x3e0000) << 2
					| (pixel & 0x3f00) << 5
					| (pixel & 0x3e) << 7);
			} else {
				pixel <<= 8;
			}

			lcd->read_buffer = pixel;
		} else {
			uint16_t val = LCD_REG(reg_index);
			lcd->read_step = !lcd->read_step;
			if (lcd->read_step) {
				cpu->bus = val >> 8;
			} else {
				cpu->bus = val & 0xFF;
			}
		}

		cpu->input = FALSE;
	}

	// Make sure timers are valid
	if (lcd->base.time > cpu->timer_c->elapsed)
		lcd->base.time = cpu->timer_c->elapsed;
	//else if (cpu->timer_c->elapsed - lcd->base.time > (2.0 / STEADY_FREQ_MIN))
	//	lcd->base.time = cpu->timer_c->elapsed - (2.0 / STEADY_FREQ_MIN);

#ifdef REAL_LCD
	if (((cpu->timer_c->elapsed - lcd->base.time) >= (1.0 / lcd->frame_rate)) && !lcd->is_drawing) {
		ColorLCD_enqueue(cpu, lcd);
		lcd->base.time += (1.0 / lcd->frame_rate);
	}

	while (cpu->timer_c->elapsed - lcd->last_draw >= lcd->line_time) {
		ColorLCD_enqueue(cpu, lcd);
	}
#else
	if (((cpu->timer_c->elapsed - lcd->base.time) >= (1.0 / lcd->frame_rate)) && !lcd->is_drawing) {
		ColorLCD_enqueue(cpu, lcd);
		lcd->base.time += (1.0 / lcd->frame_rate);
	}
#endif
}

static int read_pixel(ColorLCD_t *lcd) {
	int x = lcd->base.x % COLOR_LCD_WIDTH;
	int y = lcd->base.y % COLOR_LCD_HEIGHT;
	uint8_t *pixel_ptr = &lcd->display[PIXEL_OFFSET(x, y)];
	int pixel;
	if (LCD_REG_MASK(ENTRY_MODE_REG, BGR_MASK)) {
		pixel = (pixel_ptr[2] << 16) | (pixel_ptr[1] << 8) | pixel_ptr[0];
	} else {
		pixel = (pixel_ptr[0] << 16) | (pixel_ptr[1] << 8) | pixel_ptr[2];
	}

	return pixel;
}

static void write_pixel(ColorLCD_t *lcd, uint8_t red, uint8_t green, uint8_t blue) {
	int x = lcd->base.x;
	int y = lcd->base.y;

	if (LCD_REG_MASK(DRIVER_OUTPUT_CONTROL1_REG, FLIP_COLS_MASK)) {
		y = COLOR_LCD_HEIGHT - y - 1;
	}

	uint8_t *pixel_ptr = &lcd->display[PIXEL_OFFSET(x, y)];
	if (LCD_REG_MASK(ENTRY_MODE_REG, BGR_MASK)) {
		pixel_ptr[2] = red;
		pixel_ptr[1] = green;
		pixel_ptr[0] = blue;
	} else {
		pixel_ptr[0] = red;
		pixel_ptr[1] = green;
		pixel_ptr[2] = blue;
	}

	if (LCD_REG_MASK(ENTRY_MODE_REG, CUR_DIR_MASK)) {
		update_x(lcd, TRUE);
	} else {
		update_y(lcd, TRUE);
	}
}

static void write_pixel18(ColorLCD_t *lcd) {
	int pixel_val;
	uint8_t red, green, blue;
	if (LCD_REG_MASK(ENTRY_MODE_REG, UNPACKED_MASK)) {
		pixel_val = lcd->write_buffer & 0xfcfcfc;
		red = (pixel_val >> 18) & 0x3F;
		green = (pixel_val >> 10) & 0x3F;
		blue = (pixel_val >> 2) & 0x3F;
	} else {
		pixel_val = lcd->write_buffer & 0x3ffff;
		red = (pixel_val >> 12) & 0x3F;
		green = (pixel_val >> 6) & 0x3F;
		blue = pixel_val & 0x3F;
	}

	write_pixel(lcd, red, green, blue);
}

static void write_pixel16(ColorLCD_t *lcd) {
	int pixel_val = lcd->write_buffer;
	int red_significant_bit = pixel_val & BIT(15) ? 1 : 0;
	int blue_significant_bit = pixel_val & BIT(4) ? 1 : 0;

	uint8_t red = ((pixel_val >> 10) | red_significant_bit) & 0x3F;
	uint8_t green = (pixel_val >> 5) & 0x3F;
	uint8_t blue = ((pixel_val << 1) | blue_significant_bit) & 0x3F;

	write_pixel(lcd, red, green, blue);
}

static void update_y(ColorLCD_t *lcd, BOOL should_update) {
	if (LCD_REG_MASK(ENTRY_MODE_REG, ROW_INC_MASK)) {
		if (lcd->base.y < LCD_REG(WINDOW_HORZ_END_REG)) {
			lcd->base.y++;
			return;
		}

		// back to top of the window
		lcd->base.y = LCD_REG(WINDOW_HORZ_START_REG);
	} else {
		if (lcd->base.y > LCD_REG(WINDOW_HORZ_START_REG)) {
			lcd->base.y--;
			return;
		}

		// to bottom of the window
		lcd->base.y = LCD_REG(WINDOW_HORZ_END_REG);
	}

	if (should_update) {
		update_x(lcd, FALSE);
	}
}

static void update_x(ColorLCD_t *lcd, BOOL should_update) {
	if (LCD_REG_MASK(ENTRY_MODE_REG, COL_INC_MASK)) {
		if (lcd->base.x < LCD_REG(WINDOW_VERT_END_REG)) {
			lcd->base.x++;
			return;
		}

		// back to the beginning of the window
		lcd->base.x = LCD_REG(WINDOW_VERT_START_REG);
	} else {
		if (lcd->base.x > LCD_REG(WINDOW_VERT_START_REG)) {
			lcd->base.x--;
			return;
		}

		// to the end of the window
		lcd->base.x = LCD_REG(WINDOW_VERT_END_REG);
	}

	if (should_update) {
		update_y(lcd, FALSE);
	}
}

void ColorLCD_LCDreset(ColorLCD_t *lcd) {
	ZeroMemory(lcd, sizeof(ColorLCD_t));

	lcd->base.free = &ColorLCD_free;
	lcd->base.reset = &ColorLCD_reset;
	lcd->base.command = (devp)&ColorLCD_command;
	lcd->base.data = (devp)&ColorLCD_data;
	lcd->base.image = &ColorLCD_Image;

	lcd->base.width = COLOR_LCD_WIDTH;
	lcd->base.display_width = COLOR_LCD_WIDTH;
	lcd->base.height = COLOR_LCD_HEIGHT;
	lcd->base.contrast = 0;

	lcd->display_lines = COLOR_LCD_WIDTH;
	lcd->frame_rate = 69;
	lcd->back_porch = 2;
	lcd->front_porch = 2;
	lcd->clocks_per_line = 16;
	lcd->clock_divider = 1;
	lcd->backlight_active = TRUE;

	set_line_time(lcd);

	lcd->registers[DRIVER_CODE_REG] = DRIVER_CODE_VER;
	lcd->registers[DISPLAY_CONTROL2_REG] = 0x0202;
	lcd->registers[FRAME_RATE_COLOR_CONTROL_REG] = 0x000B;
	lcd->registers[GATE_SCAN_CONTROL_REG] = 0x2700;
	lcd->registers[PANEL_INTERFACE_CONTROL1_REG] = 0x0010;
	lcd->registers[PANEL_INTERFACE_CONTROL2_REG] = 0x0600;
	lcd->registers[PANEL_INTERFACE_CONTROL4_REG] = 0x0600;
	lcd->registers[PANEL_INTERFACE_CONTROL5_REG] = 0x0C00;
	lcd->base.bytes_per_pixel = 3;
}

void ColorLCD_reset(CPU_t *cpu) {
	ColorLCD_t *lcd = (ColorLCD_t *) cpu->pio.lcd;
	ColorLCD_LCDreset(lcd);
}

static void draw_row_floating(uint8_t *dest, int size) {
	// this should fade to in reality white
	memset(dest, 0xFF, size);
}
static void draw_row_image(ColorLCD_t *lcd, uint8_t *dest, uint8_t *src, int size) {
	uint8_t *red, *green, *blue;
	BOOL level_invert = !LCD_REG_MASK(BASE_IMAGE_DISPLAY_CONTROL_REG, LEVEL_INVERT_MASK);
	BOOL color8bit = LCD_REG_MASK(DISPLAY_CONTROL1_REG, COLOR8_MASK);

	int contrast = MAX_BACKLIGHT_LEVEL - lcd->base.contrast;
	int alpha = (contrast * 100 / MAX_BACKLIGHT_LEVEL) +0x1F;
	if (alpha > 100) {
		alpha = 100;
	}

	int contrast_color = 0x00;
	if (lcd->base.contrast == MAX_BACKLIGHT_LEVEL - 1) {
		alpha = 0;
	}
	
	int alpha_overlay = ((100 - alpha) * contrast_color / 100);
	int inverse_alpha = alpha;
	uint8_t r, g, b;
	int bits = color8bit ? 1 : 6;

	if (level_invert) {
		red = src;
		green = src + 1;
		blue = src + 2;

		BOOL flip_rows = LCD_REG_MASK(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) ? TRUE : FALSE;

		if (flip_rows) {
			red += size - 3;
			green += size - 3;
			blue += size - 3;
			for (int i = 0; i < size; i += 3) {
				r = red[-i] ^ 0x3f;
				g = green[-i] ^ 0x3f;
				b = blue[-i] ^ 0x3f;
				if (color8bit) {
					r >>= 5;
					g >>= 5;
					b >>= 5;
				}

				dest[i] = (uint8_t)(alpha_overlay + TRUCOLOR(r, bits) * inverse_alpha / 100);
				dest[i + 1] = (uint8_t)(alpha_overlay + TRUCOLOR(g, bits) * inverse_alpha / 100);
				dest[i + 2] = (uint8_t)(alpha_overlay + TRUCOLOR(b, bits) * inverse_alpha / 100);
			}
		} else {
			for (int i = 0; i < size; i += 3) {
				r = src[i] ^ 0x3f;
				g = green[i] ^ 0x3f;
				b = blue[i] ^ 0x3f;
				if (color8bit) {
					r >>= 5;
					g >>= 5;
					b >>= 5;
				}

				dest[i] = (uint8_t)(alpha_overlay + TRUCOLOR(r, bits) * inverse_alpha / 100);
				dest[i + 1] = (uint8_t)(alpha_overlay + TRUCOLOR(g, bits) * inverse_alpha / 100);
				dest[i + 2] = (uint8_t)(alpha_overlay + TRUCOLOR(b, bits) * inverse_alpha / 100);
			}
		}
	} else {
		for (int i = 0; i < size; i++) {
			uint8_t val = color8bit ? src[i] >> 5 : src[i];
			dest[i] = (uint8_t)(alpha_overlay + TRUCOLOR(val, bits) * inverse_alpha / 100);
		}
	}
}

static void draw_partial_image(ColorLCD_t *lcd, uint8_t *dest, uint8_t *src,
	int offset, int size) {

	if (offset > COLOR_LCD_WIDTH * COLOR_LCD_DEPTH) {
		offset %= COLOR_LCD_WIDTH * COLOR_LCD_DEPTH;
	}

	if (offset + size > COLOR_LCD_WIDTH * COLOR_LCD_DEPTH) {
		int right_margin_size = (COLOR_LCD_WIDTH * COLOR_LCD_DEPTH) - offset;
		int left_margin_size = size - right_margin_size;
		BOOL flip_rows = LCD_REG_MASK(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) ? TRUE : FALSE;

		if (flip_rows) {
			draw_row_image(lcd, dest + left_margin_size, src + offset, right_margin_size);
			draw_row_image(lcd, dest, src, left_margin_size);
		} else {
			draw_row_image(lcd, dest, src + offset, right_margin_size);
			draw_row_image(lcd, dest + right_margin_size, src, left_margin_size);
		}
	} else {
		draw_row_image(lcd, dest, src + offset, size);
	}
}

static void draw_nondisplay_area(uint8_t *dest, int size, int ndl_color) {
	memset(dest, ndl_color, size);
}

static void draw_row(ColorLCD_t *lcd, uint8_t *dest, uint8_t* src,
	int start_x, int display_width,
	int imgpos1, int imgoffs1, int imgsize1,
	int imgpos2, int imgoffs2, int imgsize2)
{
	uint8_t interlace_buf[COLOR_LCD_WIDTH * COLOR_LCD_DEPTH];

	int non_display_area_color = LCD_REG_MASK(BASE_IMAGE_DISPLAY_CONTROL_REG, NDL_MASK) ? 
		TRUCOLOR(0x00, 6) : TRUCOLOR(0x3F, 6);
	BOOL interlace_cols = LCD_REG_MASK(DRIVER_OUTPUT_CONTROL1_REG, INTERLACED_MASK) ? TRUE : FALSE;

	uint8_t *optr = interlace_cols ? interlace_buf : dest;

	if (start_x) {
		if (interlace_cols) {
			draw_nondisplay_area(optr, start_x, 0);
		} else {
			draw_row_floating(optr, start_x);
		}
		optr += start_x;
	}

	int n = COLOR_LCD_WIDTH * COLOR_LCD_DEPTH - start_x - display_width;
	if (imgsize1 != n && imgsize2 != n) {
		draw_nondisplay_area(optr, n, non_display_area_color);
	}

	if (imgsize1) {
		draw_partial_image(lcd, optr + imgpos1, src, imgoffs1, imgsize1);
	}

	if (imgsize2) {
		draw_partial_image(lcd, optr + imgpos2, src, imgoffs2, imgsize2);
	}

	optr += n;

	if (display_width) {
		if (interlace_cols) {
			draw_nondisplay_area(optr, display_width, 0);
		} else {
			draw_row_floating(optr, display_width);
		}
		optr += display_width;
	}

	if (interlace_cols) {
		optr = interlace_buf;
		for (int i = 0; i < COLOR_LCD_WIDTH / 2; i++, optr += 3) {
			*dest++ = optr[0];
			*dest++ = optr[1];
			*dest++ = optr[2];
			*dest++ = optr[(COLOR_LCD_DEPTH * COLOR_LCD_WIDTH) / 2];
			*dest++ = optr[(COLOR_LCD_DEPTH * COLOR_LCD_WIDTH) / 2 + 1];
			*dest++ = optr[(COLOR_LCD_DEPTH * COLOR_LCD_WIDTH) / 2 + 2];
		}
	}
}

uint8_t *ColorLCD_Image(LCDBase_t *lcdBase) {
	ColorLCD_t *lcd = (ColorLCD_t *)lcdBase;
	uint8_t *buffer = (uint8_t *)malloc(COLOR_LCD_DISPLAY_SIZE);
	ZeroMemory(buffer, COLOR_LCD_DISPLAY_SIZE);

	int p1pos, p1start, p1end, p1width, p2pos, p2start, p2end, p2width;

	if (!lcdBase->active || !lcd->backlight_active) {
		return buffer;
	}

	if (lcd->panic_mode) {
		// this is not exactly what happens in panic mode
		// but the main point is you know you fucked up
		for (int i = 0; i < COLOR_LCD_HEIGHT; i++) {
			for (int j = 0; j < COLOR_LCD_HEIGHT; j += 2) {
				buffer[PIXEL_OFFSET(j, i)] = 0xFF;
				buffer[PIXEL_OFFSET(j, i) + 1] = 0xFF;
				buffer[PIXEL_OFFSET(j, i) + 2] = 0xFF;
			}
		}
		return buffer;
	}

	int start_x = LCD_REG_MASK(GATE_SCAN_CONTROL_REG, BASE_START_MASK) * 8;
	int pixel_width = ((LCD_REG_MASK(GATE_SCAN_CONTROL_REG, BASE_NLINES_MASK) >> 8) + 1) * 8;
	if (start_x > COLOR_LCD_WIDTH) {
		start_x = COLOR_LCD_WIDTH;
	}

	if (pixel_width > COLOR_LCD_WIDTH - start_x) {
		pixel_width = COLOR_LCD_WIDTH - start_x;
	}

	int display_width = (COLOR_LCD_WIDTH - (start_x + pixel_width)) * COLOR_LCD_DEPTH;
	start_x = start_x * COLOR_LCD_DEPTH;

	if (LCD_REG_MASK(DISPLAY_CONTROL1_REG, BASEE_MASK)) {
		p2pos = 0;
		p2width = pixel_width;
		if (LCD_REG_MASK(BASE_IMAGE_DISPLAY_CONTROL_REG, SCROLL_ENABLED_MASK)) {
			p2start = lcd->base.z;
		} else {
			p2start = 0;
		}

		p1pos = p1start = p1width = 0;
	} else {
		if (LCD_REG_MASK(DISPLAY_CONTROL1_REG, SHOW_PARTIAL1_MASK)) {
			p1pos = LCD_REG_MASK(PARTIAL_IMAGE1_DISPLAY_POSITION_REG, P1_POS_MASK) % COLOR_LCD_WIDTH;
			p1start = LCD_REG_MASK(PARTIAL_IMAGE1_START_LINE_REG, P1_START_MASK) % COLOR_LCD_WIDTH;
			p1end = LCD_REG_MASK(PARTIAL_IMAGE1_END_LINE_REG, P1_END_MASK) % COLOR_LCD_WIDTH;

			p1width = p1end + 1 - p1start;
			if (p1width < 0)
				p1width += COLOR_LCD_WIDTH;

			if (p1pos > pixel_width) {
				p1pos = pixel_width;
			}

			if (p1pos + p1width > pixel_width) {
				p1width = pixel_width - p1pos;
			}
		} else {
			p1pos = p1start = p1width = 0;
		}

		if (LCD_REG_MASK(DISPLAY_CONTROL1_REG, SHOW_PARTIAL2_MASK)) {
			p2pos = LCD_REG_MASK(PARTIAL_IMAGE2_DISPLAY_POSITION_REG, P2_POS_MASK) % COLOR_LCD_WIDTH;
			p2start = LCD_REG_MASK(PARTIAL_IMAGE2_START_LINE_REG, P2_START_MASK) % COLOR_LCD_WIDTH;
			p2end = LCD_REG_MASK(PARTIAL_IMAGE2_END_LINE_REG, P2_END_MASK) % COLOR_LCD_WIDTH;

			p2width = p2end + 1 - p2start;
			if (p2width < 0) {
				p2width += COLOR_LCD_WIDTH;
			}

			if (p2pos > pixel_width) {
				p2pos = pixel_width;
			}

			if (p2pos + p2width > pixel_width) {
				p2width = pixel_width - p2pos;
			}
		} else {
			p2pos = p2start = p2width = 0;
		}
	}

	BOOL flip_rows = LCD_REG_MASK(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) ? TRUE : FALSE;
	if (flip_rows) {
		int tmp = display_width;
		display_width = start_x;
		start_x = tmp;

		p1pos = COLOR_LCD_WIDTH - (p1pos + p1width);
		p2pos = COLOR_LCD_WIDTH - (p2pos + p2width);
	}

	uint8_t *dest = buffer;
	uint8_t *src = lcd->queued_image;

	int imgpos1 = p2pos * COLOR_LCD_DEPTH;
	int imgoffs1 = p2start * COLOR_LCD_DEPTH;
	int imgsize1 = p2width * COLOR_LCD_DEPTH;
	int imgpos2 = p1pos * COLOR_LCD_DEPTH;
	int imgoffs2 = p1start * COLOR_LCD_DEPTH;
	int imgsize2 = p1width * COLOR_LCD_DEPTH;

	for (int i = 0; i < COLOR_LCD_HEIGHT; i++) {
		draw_row(lcd, dest, src,
			start_x, display_width,
			imgpos1, imgoffs1, imgsize1,
			imgpos2, imgoffs2, imgsize2);

		dest += COLOR_LCD_WIDTH * COLOR_LCD_DEPTH;
		src += COLOR_LCD_WIDTH * COLOR_LCD_DEPTH;
	}

	return buffer;
}