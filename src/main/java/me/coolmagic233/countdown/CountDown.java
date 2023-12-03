package me.coolmagic233.countdown;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import tip.utils.Api;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CountDown extends PluginBase {
    // 获取当前时间
    private static LocalDateTime now = LocalDateTime.now();

    // 获取新年时间（假设为2023年1月1日 00:00:00）
    private static LocalDateTime newYear = LocalDateTime.of(LocalDate.now(), LocalTime.now());

    // 计算时间差
    private static Duration duration = Duration.between(now, newYear);

    // 获取时间差的总秒数
    private static long seconds = duration.getSeconds();
    // 计算天数
    long days = seconds / (60 * 60 * 24);
    private static boolean start;
    private static String message = "";
    private static List<String> commands = new ArrayList<>();

    @Override
    public void onEnable(){
        saveDefaultConfig();
        commands.addAll(getConfig().getStringList("commands"));
        getServer().getCommandMap().register("me.coolmagic233.countdown.CountDown", new Command("cd") {
            @Override
            public boolean execute(CommandSender commandSender, String s, String[] strings) {
                if (commandSender.isOp()){
                    switch (strings[0]){
                        case "stop":
                            start = false;
                            commandSender.sendMessage("§a已停止倒计时任务.");
                            break;
                        case "start":
                            //cd start 2024 1 1 0 0 0
                            if (strings.length == 7){
                                if (start){
                                    commandSender.sendMessage("§c倒计时任务已经启动了! /cd stop 即可停止");
                                    return true;
                                }
                                try{
                                    int year = Integer.parseInt(strings[1]);
                                    int month = Integer.parseInt(strings[2]);
                                    int days = Integer.parseInt(strings[3]);
                                    int hour = Integer.parseInt(strings[4]);
                                    int minute = Integer.parseInt(strings[5]);
                                    int seconds = Integer.parseInt(strings[6]);
                                    newYear = LocalDateTime.of(year, month, days, hour, minute, seconds);
                                    start = true;
                                      start();
                                    commandSender.sendMessage("§a倒计时任务启动成功!");
                                    if (message.length() < 5 ){
                                        commandSender.sendMessage("§a建议 -> 请设置倒计时文本显示，否则将不会显示倒计时内容");
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    commandSender.sendMessage("§c启动任务时发生异常!可能是参数错误!");
                                }
                            }else {
                                commandSender.sendMessage("§c参数不足!");
                                return true;
                            }
                            break;
                        case "setMessage":
                            if (strings.length == 2){
                                message = strings[1];
                                commandSender.sendMessage("§a设置成功!");
                            }
                            break;
                        case "reload":
                            reload();
                            commandSender.sendMessage("§a重载成功!§c重载会停止倒计时任务!");
                            break;
                        default:break;
                    }
                }
                return false;
            }
        });
    }
    public void reload(){
        reloadConfig();
        commands.clear();
        commands.addAll(getConfig().getStringList("commands"));
        start = false;
    }
    public void start() {
        getServer().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int i) {
                if (!start){
                    this.cancel();
                    return;
                }
                String now = new SimpleDateFormat().format(new Date().getTime());
                if (now.equals(new SimpleDateFormat().format(Date.from(newYear.atZone(ZoneId.systemDefault()).toInstant())))){
                    start = false;
                    this.cancel();
                    for (Player player : getServer().getOnlinePlayers().values()) {
                        if (player.isOnline()) {
                            for (String s : commands) {
                                String[] cmd = s.split("&");
                                if ((cmd.length > 1) && ("con".equals(cmd[1]))) {
                                    getServer().dispatchCommand(getServer().getConsoleSender(), cmd[0].replace("@p", player.getName()));
                                }else {
                                    getServer().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
                                }
                            }
                        }
                    }
                }
                // 剩余秒数
                seconds %= (60 * 60 * 24);

                // 计算小时数
                long hours = seconds / (60 * 60);

                // 剩余秒数
                seconds %= (60 * 60);

                // 计算分钟数
                long minutes = seconds / 60;
                // 剩余秒数
                seconds %= 60;
                // 每秒钟更新一次时间差和倒计时信息

                // 更新时间差
                duration = Duration.between(LocalDateTime.now(), newYear);
                seconds = duration.getSeconds();

                // 转换时间差
                days = seconds / (60 * 60 * 24);
                seconds %= (60 * 60 * 24);
                hours = seconds / (60 * 60);
                seconds %= (60 * 60);
                minutes = seconds / 60;
                seconds %= 60;
                // 打印倒计时信息
                for (Player player : getServer().getOnlinePlayers().values()) {
                    if (getServer().getPluginManager().getPlugin("Tips") != null){
                        player.sendActionBar(Api.strReplace(message.replace("@day",String.valueOf(days)).replace("@hour",String.valueOf(hours)).replace("@min",String.valueOf(minutes)).replace("@s",String.valueOf(seconds)),player));
                    }else {
                        player.sendActionBar(message.replace("@day",String.valueOf(days)).replace("@hour",String.valueOf(hours)).replace("@min",String.valueOf(minutes)).replace("@s",String.valueOf(seconds)));
                    }
                }
            }
        },20);
    }
}
