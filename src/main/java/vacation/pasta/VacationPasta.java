package vacation.pasta;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class VacationPasta extends ListenerAdapter {
    private static HashMap<String, String> data = readData();

    public static void main(String[] args) throws Exception {
        String token = "YOUR_TOKEN"; // Replace with your bot's token

        JDA jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new VacationPasta())
                .setActivity(Activity.playing("STATUS_MESSAGE"))
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("enable", "enable the bot :3")
                        .addOption(OptionType.STRING, "message", "The message to be returned", true),
                Commands.slash("shutup", "stops displaying your message")
        ).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String messageContent = event.getMessage().getContentRaw();

        for(Map.Entry<String, String> e : data.entrySet()) {
            if (messageContent.contains("<@"+e.getKey()+">")) {
                MessageChannel channel = event.getChannel();
                channel.sendMessage(e.getValue()).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String user;
        switch (event.getName()){
            case "enable":
                user = Objects.requireNonNull(event.getMember()).getId();
                String message = event.getOption("message", OptionMapping::getAsString);

                //check if user has another message already
                if (data.containsKey(user)){
                    event.reply("You already have a message set up, please delete it first!")
                            .setEphemeral(true).queue();
                    break;
                }
                //saving
                data.put(user, message);
                saveData();
                //reply
                event.reply("Done successfully! :3").setEphemeral(true).queue();
                break;

            case "shutup":
                user = Objects.requireNonNull(event.getMember()).getId();

                //removing
                boolean removed = data.remove(user) == null;
                //reply
                if (removed){
                    event.reply("You... have no messages??").setEphemeral(true).queue();
                    break;
                }else {
                    saveData();
                    event.reply("Oke... :c").setEphemeral(true).queue();
                    break;
                }
            default:
                event.reply("Invalid command!")
                        .setEphemeral(true).queue();
        }
    }

    public static void saveData(){
        try {
            Properties properties = new Properties();
            properties.putAll(data);
            properties.store(new FileOutputStream("saves.properties"), null);
        } catch (IOException ioException){
            throw new RuntimeException("Something went wrong: " + ioException);
        }
    }

    public static HashMap<String, String> readData(){
        HashMap<String, String> data = new HashMap<>();
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("saves.properties"));

            for (String key : properties.stringPropertyNames()) {
                data.put(key, properties.get(key).toString());
            }
            return data;
        }catch (FileNotFoundException file){
            return new HashMap<>();
        }catch (IOException ioException){
            throw new RuntimeException("Something went wrong: " + ioException);
        }
    }
}
