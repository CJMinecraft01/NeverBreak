function initializeCoreMod() {
    return {
        'override-is-blocking': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.PlayerEntity',
                'methodName': 'func_184590_k', // hurtCurrentlyUsedShield
                'methodDesc': '(F)V'
            },
            'transformer': function (method) {
                var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
                var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var newInstructions = new InsnList();

                var escape = new LabelNode();

                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
                newInstructions.add(ASM.buildMethodCall(
                    "cjminecraft/neverbreak/AsmHooks",
                    "hurtCurrentlyUsedShield",
                    "(Lnet/minecraft/entity/player/PlayerEntity;F)Z",
                    ASM.MethodType.STATIC));
                newInstructions.add(new JumpInsnNode(Opcodes.IFEQ, escape));
                newInstructions.add(new InsnNode(Opcodes.RETURN));
                newInstructions.add(escape);

                method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
                return method;
            }
        }
    }
}