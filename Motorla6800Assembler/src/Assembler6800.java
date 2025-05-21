import java.util.*;

public class Assembler6800 {

    static class Instruction {
        String label;
        String opcode;
        String operand;
        String originalLine;
        int address;
        String objectCode;

        Instruction(String label, String opcode, String operand, String originalLine) {
            this.label = label;
            this.opcode = opcode;
            this.operand = operand;
            this.originalLine = originalLine;
        }
    }

    static Map<String, Integer> opcodeTable = new HashMap<>();
    static {
        opcodeTable.put("LDAA_IMM", 0x86);
        opcodeTable.put("STAA_DIR", 0x97);
        opcodeTable.put("DECA", 0x4A);
        opcodeTable.put("BNE", 0x26);
        opcodeTable.put("JMP", 0x7E);
        opcodeTable.put("NOP", 0x01);
    }

    static Map<String, Integer> labelTable;
    static List<Instruction> instructions;
    static int origin;

    public static String assemble(String codeText) {
        labelTable = new HashMap<>();
        instructions = new ArrayList<>();
        origin = 0x8000; // Varsayılan ORG

        String[] lines = codeText.split("\\r?\\n");

        // 1. Geçiş: adres hesaplama ve etiket toplama
        int currentAddress = origin;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String original = line;

            if (line.startsWith("ORG")) {
                String operand = line.split(" ")[1].toUpperCase().replace("$", "0x");
                origin = Integer.decode(operand);
                currentAddress = origin;
                continue;
            }

            if (line.equals("END")) break;

            String label = null, opcode = null, operand = null;

            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                label = parts[0].trim();
                line = parts[1].trim();
                labelTable.put(label, currentAddress);
            }

            String[] tokens = line.split(" ", 2);
            opcode = tokens[0];
            if (tokens.length > 1) operand = tokens[1].trim();

            Instruction instr = new Instruction(label, opcode, operand, original);
            instr.address = currentAddress;

            // Komut boyutu tahmini
            if (opcode.equals("DECA") || opcode.equals("NOP")) {
                currentAddress += 1;
            } else if (opcode.equals("LDAA") && operand.startsWith("#")) {
                currentAddress += 2;
            } else if (opcode.equals("STAA")) {
                currentAddress += 2;
            } else if (opcode.equals("BNE")) {
                currentAddress += 2;
            } else if (opcode.equals("JMP")) {
                currentAddress += 3;
            }

            instructions.add(instr);
        }

        // 2. Geçiş: nesne kodu üretme
        for (Instruction instr : instructions) {
            String opc = instr.opcode;
            String opr = instr.operand;
            String key;
            String obj = "";

            if (opc.equals("LDAA") && opr.startsWith("#")) {
                key = "LDAA_IMM";
                int val = Integer.decode(opr.substring(1).toUpperCase().replace("$", "0x"));
                obj = String.format("%02X %02X", opcodeTable.get(key), val);
            } else if (opc.equals("STAA")) {
                key = "STAA_DIR";
                int addr = Integer.decode(opr.toUpperCase().replace("$", "0x"));
                obj = String.format("%02X %02X", opcodeTable.get(key), addr);
            } else if (opc.equals("DECA")) {
                obj = String.format("%02X", opcodeTable.get("DECA"));
            } else if (opc.equals("BNE")) {
                int targetAddr = labelTable.get(opr);
                int offset = targetAddr - (instr.address + 2);
                obj = String.format("%02X %02X", opcodeTable.get("BNE"), offset & 0xFF);
            } else if (opc.equals("JMP")) {
                int target = labelTable.get(opr);
                obj = String.format("%02X %04X", opcodeTable.get("JMP"), target);
            } else if (opc.equals("NOP")) {
                obj = String.format("%02X", opcodeTable.get("NOP"));
            }

            instr.objectCode = obj;
        }

        // Sonuç oluşturma
        StringBuilder result = new StringBuilder();
        for (Instruction instr : instructions) {
            result.append(String.format("%-15s => %s\n", instr.originalLine, instr.objectCode));
        }

        return result.toString();
    }

    // Test için main metodu
    public static void main(String[] args) {
        String sampleCode = """
            ORG $8000
            LDAA #$05
            LOOP: STAA $30
            DECA
            BNE LOOP
            JMP DONE
            DONE: NOP
            END
            """;

        String result = assemble(sampleCode);
        System.out.println(result);
    }
}
