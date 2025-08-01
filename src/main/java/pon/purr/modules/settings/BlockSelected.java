package pon.purr.modules.settings;

import pon.purr.config.ConfigManager;
import pon.purr.modules.Parent;
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

public class BlockSelected {
    private ConfigManager config;
    private String moduleName;
    private String key;

    public BlockSelected(Parent module) {
        this.config = module.getConfig();
        this.moduleName = module.getName();
        this.key = "target_blocks";

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal(moduleName)
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
                                List<String> ids =  config.get(key);
                                if (!ids.isEmpty()) {
                                    for (String blockId : (List<String>) config.get(key)) {
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

    private void onBlocksAdd(ArrayList<String> ids) {
        ArrayList<String> listOne = config.get(key, new ArrayList<>());

        Set<String> set = new LinkedHashSet<>(listOne);
        set.addAll(ids);

        ArrayList<String> combinedList = new ArrayList<>(set);

        config.set(key, combinedList);
    }

    private void onBlocksRemove(ArrayList<String> ids) {
        ArrayList<String> blocks = config.get(key, new ArrayList<>());

        for (String id : ids) {
            if (blocks.contains(id)) {
                blocks.remove(id);
            }
        }

        config.set(key, blocks);
    }

    private void onBlocksClear() {
        config.set(key, new ArrayList<>());
    }

    public List<String> getValue() {
        return config.get(key, new ArrayList<>());
    }

    private String convertId(String id) {
        return id.replace("block.", "").replace(".", ":");
    }
}
