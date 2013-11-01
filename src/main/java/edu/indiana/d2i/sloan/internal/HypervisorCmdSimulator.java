package edu.indiana.d2i.sloan.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class HypervisorCmdSimulator extends CommandSimulator {
	public static final String SECURE_MODE_STR = "secure";
	public static final String MAINTAIN_MODE_STR = "maintain";

	public static enum ERROR_STATE {
		INVALID_INPUT_ARGS, IMAGE_NOT_EXIST, NOT_ENOUGH_CPU, NOT_ENOUGH_MEM, IO_ERR, VM_NOT_EXIST, FIREWALL_POLICY_NOT_EXIST, INVALID_VM_MODE
	}

	/* key is error type enum, value is error code */
	public static Map<ERROR_STATE, Integer> ERROR_CODE;

	static {
		ERROR_CODE = new HashMap<ERROR_STATE, Integer>() {
			{
				put(ERROR_STATE.INVALID_INPUT_ARGS, 1);
				put(ERROR_STATE.IMAGE_NOT_EXIST, 2);
				put(ERROR_STATE.NOT_ENOUGH_CPU, 3);
				put(ERROR_STATE.NOT_ENOUGH_MEM, 4);
				put(ERROR_STATE.IO_ERR, 5);
				put(ERROR_STATE.VM_NOT_EXIST, 6);
				put(ERROR_STATE.FIREWALL_POLICY_NOT_EXIST, 7);
				put(ERROR_STATE.INVALID_VM_MODE, 8);
			}
		};

	}

	public static boolean checkFileExist(String filePath) {
		return new File(filePath).exists();
	}

}
