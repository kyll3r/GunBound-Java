package br.com.gunbound.emulator.packets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import br.com.gunbound.emulator.packets.readers.AuthReader;
import br.com.gunbound.emulator.packets.readers.CashUpdateReader;
import br.com.gunbound.emulator.packets.readers.KeepAlive;
import br.com.gunbound.emulator.packets.readers.LoginReader;
import br.com.gunbound.emulator.packets.readers.MessageBcmReader;
import br.com.gunbound.emulator.packets.readers.UserIdReader;
import br.com.gunbound.emulator.packets.readers.lobby.LobbyChatReader;
import br.com.gunbound.emulator.packets.readers.lobby.LobbyJoin;
import br.com.gunbound.emulator.packets.readers.room.JoinRoomReader;
import br.com.gunbound.emulator.packets.readers.room.RoomCreateReader;
import br.com.gunbound.emulator.packets.readers.room.RoomDetailReader;
import br.com.gunbound.emulator.packets.readers.room.RoomGameStartReader;
import br.com.gunbound.emulator.packets.readers.room.RoomListReader;
import br.com.gunbound.emulator.packets.readers.room.RoomSelectTankReader;
import br.com.gunbound.emulator.packets.readers.room.RoomTunnelReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeCapacityReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeItemReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeMapReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeOptionReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeTeamReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomChangeTitleReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomCommandReader;
import br.com.gunbound.emulator.packets.readers.room.change.RoomUserReadyReader;
import br.com.gunbound.emulator.packets.readers.room.gameplay.GamePlayerDeadReader;
import br.com.gunbound.emulator.packets.readers.room.gameplay.GamePlayerResurrectReader;
import br.com.gunbound.emulator.packets.readers.room.gameplay.GameResultEndJewel;
import br.com.gunbound.emulator.packets.readers.room.gameplay.GameResultReader;
import br.com.gunbound.emulator.packets.readers.room.gameplay.GameReturnRoomResultReader;
import br.com.gunbound.emulator.packets.readers.shop.AvatarPlayerBuyReader;
import br.com.gunbound.emulator.packets.readers.shop.AvatarPlayerConfirmReader;
import br.com.gunbound.emulator.packets.readers.shop.AvatarPlayerOwnReader;
import br.com.gunbound.emulator.packets.readers.shop.AvatarPlayerSortReader;
import io.netty.channel.ChannelHandlerContext;

public class OpcodeReaderFactory {
	private static final Map<Integer, BiConsumer<ChannelHandlerContext, byte[]>> readers = new HashMap<>();

	static {
		readers.put(0x1000, AuthReader::read); //autenticação
		readers.put(0x1010, LoginReader::read); //Login GBServ
        readers.put(0x1020, UserIdReader::read); // Buscar detalhes de um usuário**
		readers.put(0x2000, LobbyJoin::read); //entrar em um Lobby
		readers.put(0x2010, LobbyChatReader::read);// Ler as mensagens originadas no chat
		readers.put(0x6100, CashUpdateReader::read);// cash update
		
		
		//room
		readers.put(0x2120, RoomCreateReader::read); // Criar Sala
		readers.put(0x2110, JoinRoomReader::read); // Entra em um room
		readers.put(0x3200, RoomSelectTankReader::read); // seleciona o mobile
		readers.put(0x3102, RoomChangeItemReader::read); //mudar estados dos itens na sala
		readers.put(0x3101, RoomChangeOptionReader::read); //Mudar opções da sala
		readers.put(0x3100, RoomChangeMapReader::read); //altera mapa na sala
        readers.put(0x3103, RoomChangeCapacityReader::read); // Mudar capacidade máxima da sala
        readers.put(0x3104, RoomChangeTitleReader::read); //mudar nome da sala
        readers.put(0x3210, RoomChangeTeamReader::read);  //Mudar de time na sala
        readers.put(0x3230, RoomUserReadyReader::read);  //Esta pronto (ready) ou não
        readers.put(0x2100, RoomListReader::read); // Solicitar lista de salas
        readers.put(0x2104, RoomDetailReader::read); //detalhe da sala na lista de sala
		

        readers.put(0x3430, RoomGameStartReader::read); // iniciar o jogo
        
        
        readers.put(0x4100, GamePlayerDeadReader::read); // morto!
        readers.put(0x4104, GamePlayerResurrectReader::read); // Vivo!
        readers.put(0x4412, GameResultReader::read); // resultado
        readers.put(0x4200, GameResultEndJewel::read); // resultado Jewel
        readers.put(0x3232, GameReturnRoomResultReader::read); // resultado
        
        readers.put(0x4500, RoomTunnelReader::read);//TUNEL
        readers.put(0x5100, RoomCommandReader::read);//Le os comandos
        
        
        readers.put(0x6000, AvatarPlayerOwnReader::read);//Avatar Prop
        readers.put(0x600A, AvatarPlayerSortReader::read);//Avatar (Re)Order
        readers.put(0x600E, AvatarPlayerConfirmReader::read);//Avatar Confirm
        readers.put(0x6020, AvatarPlayerBuyReader::read);//Avatar Buying
        
        
        readers.put(0x5010, MessageBcmReader::read);//Broadcast msg
        

        readers.put(0x0000, KeepAlive::read);  //KeepAlive
	}

	public static BiConsumer<ChannelHandlerContext, byte[]> getReader(int opcode) {
		return readers.get(opcode);
	}
}
