function initializeCoreMod() {
    return {
        'override-is-blocking': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.LivingEntity',
                'methodName': 'func_184585_cz', // isBlocking
                'methodDesc': '()Z'
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
                newInstructions.add(ASM.buildMethodCall(
                    "cjminecraft/neverbreak/AsmHooks",
                    "isBlocking",
                    "(Lnet/minecraft/entity/LivingEntity;)Z",
                    ASM.MethodType.STATIC));
                newInstructions.add(new JumpInsnNode(Opcodes.IFEQ, escape));
                newInstructions.add(new InsnNode(Opcodes.ICONST_0));
                newInstructions.add(new InsnNode(Opcodes.IRETURN));
                newInstructions.add(escape);

                method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);
                return method;
            }
        }
    }
}