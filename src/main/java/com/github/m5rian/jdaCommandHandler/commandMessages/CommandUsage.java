package com.github.m5rian.jdaCommandHandler.commandMessages;

import com.github.m5rian.jdaCommandHandler.CommandContext;
import com.github.m5rian.jdaCommandHandler.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author Marian
 * This class is used when sending a command usage from the {@link com.github.m5rian.jdaCommandHandler.CommandHandler}.
 * You can set up a preset with the {@link CommandUsageFactory}.
 */
public class CommandUsage {
    // Base message
    private String message;
    private final EmbedBuilder embed;
    // Command usages
    private Function<CommandEvent, String> text;
    private Function<CommandEvent, String> description;
    private Function<CommandEvent, MessageEmbed.Field> field;
    // Other
    private final CommandContext ctx;
    private final boolean reply; // Should the message be a reply?
    private Class[] classes; // Classes to get the usage from

    public CommandUsage(String message, Function<CommandEvent, String> text, EmbedBuilder embed, Function<CommandEvent, String> description, Function<CommandEvent, MessageEmbed.Field> field,
                        CommandContext ctx, boolean reply) {
        // Base message
        this.message = message;
        this.embed = embed;
        // Command usages
        this.text = text;
        this.description = description;
        this.field = field;
        // Other
        this.ctx = ctx;
        this.reply = reply;
    }

    public CommandUsage setCommands(Class... classes) {
        this.classes = classes;
        return this;
    }

    public void send() {
        // Add command usages
        for (Class clazz : classes) { // Go through all classes
            for (Method method : clazz.getMethods()) { // Go through all methods of the class
                // Command annotation is present
                if (method.isAnnotationPresent(CommandEvent.class)) {
                    final CommandEvent commandInfo = method.getAnnotation(CommandEvent.class); // Get info about command

                    // Normal text is used to display commands
                    if (this.text != null) {
                        final String usage = this.text.apply(commandInfo); // Get usage for command
                        message += "\n" + usage; // Append command usage in message
                    }

                    // Use description to display commands
                    if (this.description != null) {
                        final String usage = this.description.apply(commandInfo); // Get usage for command
                        embed.appendDescription("\n" + usage); // Append command usage to description
                    }
                    // Use fields to display commands
                    else if (this.field != null) {
                        final MessageEmbed.Field usage = this.field.apply(commandInfo); // Get usage for command
                        embed.addField(usage); // Add command usage as field
                    }
                }
            }
        }

        final MessageChannel channel = this.ctx.getChannel(); // Get channel
        final Message msg = ctx.getEvent().getMessage(); // Get message from author

        // Reply
        if (this.reply) {
            if (!this.message.isEmpty() && this.embed.isEmpty()) msg.reply(this.message).queue();
            if (!this.message.isEmpty() && !this.embed.isEmpty()) msg.reply(this.message).embed(this.embed.build()).queue();
            if (this.message.isEmpty() && !this.embed.isEmpty()) msg.reply(this.embed.build()).queue();
        }
        // Don't reply
        else {
            if (!this.message.isEmpty() && this.embed.isEmpty()) channel.sendMessage(this.message).queue();
            if (!this.message.isEmpty() && !this.embed.isEmpty()) channel.sendMessage(this.message).embed(this.embed.build()).queue();
            if (this.message.isEmpty() && !this.embed.isEmpty()) channel.sendMessage(this.embed.build()).queue();
        }
    }

}