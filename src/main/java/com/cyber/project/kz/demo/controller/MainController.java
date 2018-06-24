package com.cyber.project.kz.demo.controller;

import com.cyber.project.kz.demo.dto.GameServerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


@RestController
public class MainController {

    private String cmdStart = "docker run -it --user csgo -d -p 27015:27015 -p 27015:27015/udp -v /home/csgo:/home/csgo --name cont csgo-con /bin/bash -c \"cd /home/csgo/csgo; ./srcds_run -game csgo -console -port 27015 -tickrate 128 -usercon +game_type 0 +game_mode 1 +mapgroup mg_bomb +map de_dust2\"";
    private String cmdStop = "docker stop cont && docker rm cont";


    @RequestMapping(value = "/")
    public String test(){return "test";}

    @PostMapping("/create")
    public String CreateServer(@RequestBody GameServerInfo gameServerInfo){
        String responce = gameServerInfo.getCreatorSteamId();
        int maxPlayers = gameServerInfo.getMaxPlayers();
        String[] players = gameServerInfo.getPlayers().split(",");

        for (String player:players) {
            responce+=player;
        }
        boolean serverInfo = ServerInfoWriter(players,gameServerInfo.getCreatorSteamId());
        boolean serverRun = ServerScriptRunner("27015",cmdStart);
        if(serverInfo && serverRun) {
            return responce;
        }
        return "что-то пошло не так :)";
    }

    @GetMapping("/stop")
    public String StopServer(){
        ServerScriptRunner("27015",cmdStop);
        return "";
    }


    private Boolean ServerInfoWriter(String[] players,String admin){
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File logFile = new File(admin+"_"+players.length+"_"+timeLog);

            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            for (String player:players
                    ) {
                writer.write(player);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    private boolean ServerScriptRunner(String serverPort,String cmdCommand){
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmdCommand});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
                return true;
            }
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }
        return false;
    }
}

