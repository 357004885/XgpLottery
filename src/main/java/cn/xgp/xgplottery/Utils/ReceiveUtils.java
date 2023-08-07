package cn.xgp.xgplottery.Utils;

import cn.xgp.xgplottery.Gui.Impl.Manage.LotteryManageGui;
import cn.xgp.xgplottery.Gui.Impl.Manage.LotterySetting;
import cn.xgp.xgplottery.Gui.Impl.Manage.LotterySettings.LoreSetting;
import cn.xgp.xgplottery.Gui.LotteryGui;
import cn.xgp.xgplottery.Gui.PoolGui;
import cn.xgp.xgplottery.Listener.GetNameListener;
import cn.xgp.xgplottery.Lottery.Award;
import cn.xgp.xgplottery.Lottery.CumulativeRewards;
import cn.xgp.xgplottery.Lottery.Lottery;
import cn.xgp.xgplottery.XgpLottery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ReceiveUtils {

    public static Future<String> getInput(Player player) {
        return getInput(player, 20);
    }

    public static Future<String> getInput(Player player, int time) {
        CompletableFuture<String> future = new CompletableFuture<>();
        GetNameListener listener = new GetNameListener(player, future, time);
        Bukkit.getPluginManager().registerEvents(listener, XgpLottery.instance);
        return future;
    }


    public static void createLottery(Player player) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.RED + "[XgpLottery]" + ChatColor.GREEN + LangUtils.CreateLottery);
            player.sendMessage(ChatColor.RED + LangUtils.DontUseColor);
            try {
                String name = getInput(player).get(15, TimeUnit.SECONDS);
                name = name.trim();
                name = ChatColor.stripColor(name);
                if (name.equals("cancel")) {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    return;
                }
                if (XgpLottery.lotteryList.containsKey(name)) {
                    player.sendMessage(ChatColor.RED + LangUtils.LotteryAlreadyExists);
                } else if (!name.isEmpty()) {
                    XgpLottery.lotteryList.put(name, Lottery.getDefaultLottery(name));
                    player.sendMessage(ChatColor.YELLOW + LangUtils.CreateLotterySuccessfully.replace("%name%", name));
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {
                Inventory inventory = new LotteryManageGui().getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void receiveWeight(Player player, Award award, PoolGui returnGui) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + LangUtils.ReceiveWeight);
            try {
                String weight = getInput(player).get(15, TimeUnit.SECONDS);
                weight = ChatColor.stripColor(weight).trim();
                if (weight != null && Integer.parseInt(weight) >= 0) {
                    award.setWeight(Integer.parseInt(weight));
                    player.sendMessage(ChatColor.GREEN + LangUtils.ChangeWeightSuccessfully + weight + "!");
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = returnGui.refresh();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMaxTime(Player player, Lottery lottery) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + LangUtils.SetMaxTime);
            try {
                String times = getInput(player).get(15, TimeUnit.SECONDS);
                times = ChatColor.stripColor(times).trim();
                lottery.setMaxTime(Integer.parseInt(times));
                player.sendMessage(ChatColor.GREEN + LangUtils.ChangeTimeSuccessfully + times + "!");
                SerializeUtils.saveLotteryData();
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = new LotterySetting(lottery.getName()).getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setValue(Player player, LotterySetting lotterySetting) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + LangUtils.SetValue);
            try {
                String value = getInput(player).get(15, TimeUnit.SECONDS);
                value = ChatColor.stripColor(value).trim();
                if (value != null) {
                    lotterySetting.getLottery().setValue(Integer.parseInt(value));
                    player.sendMessage(ChatColor.GREEN + LangUtils.SetValueSuccessfully + value + "!");
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = lotterySetting.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addCommand(Player player, LotteryGui returnGui, Award award) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入要添加的命令，这个命令会以控制台执行，支持占位符！ 不要使用/开头，输入cancel取消");
            try {
                String command = getInput(player, 35).get(30, TimeUnit.SECONDS);
                if (command != null) {
                    if ("cancel".equals(ChatColor.stripColor(command))) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else {
                        award.getCommands().add(command);
                        player.sendMessage(ChatColor.GREEN + "添加指令成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {
                Inventory inventory = returnGui.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAwardDisplayName(Player player, LotteryGui returnGui, Award award) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入奖品的展示名，颜色代码：&，输入cancel取消");
            try {
                String displayName = getInput(player, 30).get(35, TimeUnit.SECONDS);
                if (displayName != null) {
                    displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                    if ("cancel".equals(ChatColor.stripColor(displayName))) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else {
                        award.setDisplayName(displayName);
                        player.sendMessage(ChatColor.GREEN + "设置成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {

                Inventory inventory = returnGui.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delCommand(Player player, LotteryGui returnGui, Award award) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入要删除的命令编号，输入cancel取消");
            for (String cmd : award.getCommandsString()) {
                player.sendMessage(ChatColor.GREEN + cmd);
            }
            try {
                String index = getInput(player).get(15, TimeUnit.SECONDS);
                index = ChatColor.stripColor(index).trim();
                if (index != null) {
                    if ("cancel".equals(index)) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else if (Integer.parseInt(index) - 1 >= award.getCommands().size()) {
                        player.sendMessage(ChatColor.GREEN + "错误的序号");
                    } else {
                        int i = Integer.parseInt(index);
                        award.getCommands().remove(i - 1);
                        player.sendMessage(ChatColor.GREEN + "删除成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {

                Inventory inventory = returnGui.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setKeyName(Player player, LotterySetting lotterySetting, boolean isKey) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入展示名，颜色代码：&，输入cancel取消");
            try {
                String displayName = getInput(player, 30).get(35, TimeUnit.SECONDS);
                if (displayName != null) {
                    displayName = ChatColor.translateAlternateColorCodes('&', displayName);
                    if ("cancel".equals(ChatColor.stripColor(displayName))) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else {
                        if (isKey) {
                            lotterySetting.getLottery().setKeyName(displayName);
                        } else {
                            lotterySetting.getLottery().setTicketName(displayName);
                        }
                        player.sendMessage(ChatColor.GREEN + "设置成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {
                Inventory inventory = lotterySetting.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLimitedTime(Player player, LotterySetting lotterySetting) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入抽奖上限值，为0则取消限制。输入cancel取消");
            try {
                String times = getInput(player).get(15, TimeUnit.SECONDS);
                times = ChatColor.stripColor(times).trim();
                if (times != null) {
                    lotterySetting.getLottery().setLimitedTimes(Integer.parseInt(times));
                    player.sendMessage(ChatColor.GREEN + LangUtils.ChangeTimeSuccessfully + times + "!");
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = lotterySetting.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addLore(Player player, LoreSetting loreSetting) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入要添加的lore,颜色符号 &,输入cancel取消");
            try {
                String newLore = getInput(player, 35).get(30, TimeUnit.SECONDS);
                if (newLore != null) {
                    newLore = ChatColor.translateAlternateColorCodes('&', newLore);
                    if ("cancel".equals(ChatColor.stripColor(newLore))) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else {
                        loreSetting.getLoreList().add(newLore);
                        player.sendMessage(ChatColor.GREEN + "添加成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {
                Inventory inventory = loreSetting.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delLore(Player player, LoreSetting loreSetting) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入要删除的lore编号，输入cancel取消");
            for (int i = 0; i < loreSetting.getLoreList().size(); i++) {
                player.sendMessage(ChatColor.BLUE + String.valueOf(i + 1) + ":" + loreSetting.getLoreList().get(i));
            }
            try {
                String index = getInput(player).get(15, TimeUnit.SECONDS);
                index = ChatColor.stripColor(index).trim();
                if (index != null) {
                    if ("cancel".equals(index)) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    } else if (Integer.parseInt(index) - 1 >= loreSetting.getLoreList().size()) {
                        player.sendMessage(ChatColor.GREEN + "错误的序号");
                    } else {
                        int i = Integer.parseInt(index);
                        loreSetting.getLoreList().remove(i - 1);
                        player.sendMessage(ChatColor.GREEN + "删除成功!");
                    }
                    SerializeUtils.saveLotteryData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = loreSetting.getInventory();
                player.getScheduler().run(XgpLottery.instance, task2 -> player.openInventory(inventory), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createRewardGift(Player player, LotteryGui returnGui) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入要创建礼包的依据奖池名称,输入cancel取消");
            try {
                String lotteryName = getInput(player).get(15, TimeUnit.SECONDS);
                lotteryName = ChatColor.stripColor(lotteryName).trim();
                if (lotteryName.equals("cancel")) {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                    return;
                }
                if (!lotteryName.isEmpty() && XgpLottery.lotteryList.containsKey(lotteryName)) {
                    player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入礼包的名称,不能重复,输入cancel取消");
                    String name = getInput(player).get(15, TimeUnit.SECONDS);
                    name = ChatColor.stripColor(name).trim();
                    if (name.equals("cancel")) {
                        player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                        return;
                    }
                    if (CumulativeRewards.getByName(name) != null) {
                        player.sendMessage(ChatColor.RED + "这个名称已存在，不能重复");
                        return;
                    }
                    XgpLottery.rewards.add(new CumulativeRewards(lotteryName, name));
                    SerializeUtils.saveRewardData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }

            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } finally {
                Inventory inventory = returnGui.getInventory();
                player.openInventory(inventory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setTimes(Player player, LotteryGui returnGui, Consumer<Integer> set) {
        player.closeInventory();
        try {
            player.sendMessage(ChatColor.GOLD + "[XgpLottery]" + ChatColor.GREEN + "请输入次数。输入cancel取消");
            try {
                String times = getInput(player).get(15, TimeUnit.SECONDS);
                times = ChatColor.stripColor(times).trim();
                if (times != null) {
                    set.accept(Integer.parseInt(times));
                    player.sendMessage(ChatColor.GREEN + LangUtils.ChangeTimeSuccessfully + times + "!");
                    SerializeUtils.saveRewardData();
                } else {
                    player.sendMessage(ChatColor.RED + LangUtils.WrongType);
                }
            } catch (TimeoutException e) {
                player.sendMessage(ChatColor.RED + LangUtils.TimeOut);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + LangUtils.WrongType);
            } finally {
                Inventory inventory = returnGui.getInventory();
                player.openInventory(inventory);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
