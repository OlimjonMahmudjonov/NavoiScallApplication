package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.request.CommandsRequest;
import uz.tenzorsoft.scaleapplication.repository.CommandsRepository;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;

import java.util.Optional;

// CommansServiceImpl.java
@Service
@RequiredArgsConstructor
public class  CommandsService {

    private final CommandsRepository commandsRepository;
    private final ButtonController buttonController;

    public CommandsEntity saveOrUpdateCommands(CommandsRequest newCommand) {
        if (newCommand == null) return null;
        // Find by scaleId in the database (CommandsEntity type)
        CommandsEntity commands = new CommandsEntity();
        commands.setServerId(newCommand.getId());
        commands.setScaleId(newCommand.getScaleId());
        commands.setOpenGate1(newCommand.getOpenGate1());
        commands.setCloseGate1(newCommand.getCloseGate1());
        commands.setWeighing(newCommand.getWeighing());
        commands.setOpenGate2(newCommand.getOpenGate2());
        commands.setCloseGate2(newCommand.getCloseGate2());
        commandsRepository.save(commands);
        handleCommands(commands);
        return commands;
    }

    private void handleCommands(CommandsEntity commands) {
        buttonController.handleServerCommands(commands);
    }

}

