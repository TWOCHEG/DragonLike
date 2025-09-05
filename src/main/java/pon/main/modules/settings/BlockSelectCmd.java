package pon.main.modules.settings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.*;

public class BlockSelectCmd extends Setting<List<String>> {
    public BlockSelectCmd(String name, String cmdName) {
        this(name, cmdName, new ArrayList());
    }
    public BlockSelectCmd(String name, String cmdName, List<String> defaultValues) {
        super(name, defaultValues);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal(cmdName)
                    .then(ClientCommandManager.literal("blocksList")
                        .then(ClientCommandManager.literal("clear")
                            .executes(context -> {
                                onBlocksClear();
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                                    Text.literal("blocks list is clear")
                                );
                                return 1;
                            })
                        )
                        .then(ClientCommandManager.literal("list")
                            .executes(context -> {
                                List<String> ids = getValue();
                                if (!ids.isEmpty()) {
                                    for (String blockId : getValue()) {
                                        Text blockName = getBlockName(blockId);
                                        if (blockName != null) {
                                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(blockName);
                                        } else {
                                            sendMessage("unknown block: " + blockId);
                                        }
                                    }
                                } else {
                                    sendMessage("blocks list is empty");
                                }
                                return 1;
                            })
                        )
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                .executes(context -> {
                                    BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);
                                    BlockState blockState = blockArg.getBlockState();
                                    String blockId = blockState.getBlock().getTranslationKey();

                                    onBlocksAdd(new ArrayList<>(List.of(convertId(blockId))));

                                    sendMessage("added:");
                                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(getBlockName(blockId));

                                    return 1;
                                })
                            )
                        )
                        .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                .executes(context -> {
                                    BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);
                                    BlockState blockState = blockArg.getBlockState();
                                    String blockId = blockState.getBlock().getTranslationKey();

                                    onBlocksRemove(new ArrayList<>(List.of(convertId(blockId))));

                                    sendMessage("remove:");
                                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(getBlockName(blockId));

                                    return 1;
                                })
                            )
                        )
                    )
            );
        });
    }

    private void sendMessage(String text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
            Text.literal(text)
        );
    }

    private Text getBlockName(String blockId) {
        String idString = convertId(blockId);
        Identifier id = Identifier.of(idString);
        Block block = Registries.BLOCK.get(id);
        if (block != Blocks.AIR) {
            return block.getName();
        } else {
            return null;
        }
    }

    public void onBlocksAdd(ArrayList<String> ids) {
        List<String> newList = new ArrayList<>(getValue());
        newList.addAll(ids);
        setValue(newList);
    }

    private void onBlocksRemove(ArrayList<String> ids) {
        List<String> newList = new ArrayList<>(getValue());
        newList.removeAll(ids);
        setValue(newList);
    }

    private void onBlocksClear() {
        setValue(new ArrayList<>());
    }

    public static String convertId(String id) {
        return id.replace("block.", "").replace(".", ":");
    }
}
