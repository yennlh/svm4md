package preprocessing;

public class TypeManager {
	private static final String VAL = "val";
	private static final String REG = "reg";
	private static final String API = "api";
	private static final String ASM = "asm";

	public static String getTypeName(String pType) {
		// 0x00415ede\ndecl 0x8(%ebp)
		// label="0x00421a85\nmovl -4(%ebp), %esi"
		// label="0x00421a64\nmovl $0xffff0000<UINT32>, %ebx"
		// label="0x00414ae0\ncall 0x00416499"
		return pType;

//		switch (pType.charAt(0)) {
//		case '%':
//			return REG;
//		case '$':
//			return VAL;
//		default:
//			if (pType.indexOf('@') > -1) {
//				return API;
//			} else if (pType.indexOf('%') > -1) {
//				// "(((\\-)?[0-9]+)|0x[0-9a-f]+)\\(%[a-z]+\\)"
//				return REG;
//			} else if (InstructionMap.isDecimal(pType) || InstructionMap.isHex(pType.substring(2, pType.length()))) {
//				return VAL;
//			}
//
//			// System.err.println("%%%% Can not get the type of operand: " +
//			// pType);
//			return ASM;
//		}
	}
}
