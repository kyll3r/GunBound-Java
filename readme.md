# GunBound Java Emulator - Thor's Hammer

## About the Project

This project is a server emulator for the classic game **GunBound**, focusing on the Thor's Hammer version with **_Ex-Item_** and **_Power User_**. Developed in Java, it utilizes the high-performance Netty networking framework to handle asynchronous communication with the game clients.

The goal is to recreate the original server experience, allowing players to connect, join rooms, and participate in matches. The project is under active development and open to community contributions.

## Implemented Features
✅ Broker Server (GunBoundBrokerServer): Lists the available game servers to clients.

✅ Game Server (GunBoundGameServer): Where all the in-game logic takes place.

✅ Authentication and Login: Handles login and password validation with both static and dynamic encryption.

✅ Lobby (Channels): Manages multiple channels, player entry/exit, and lobby chat.

✅ Room Management: Creation, listing (with pagination), and joining of game rooms.

✅ Room Configuration: Change map, title, capacity, teams.

✅ Basic Gameplay: Match start and real-time communication between players through packet 0x4500 (tunnel fully function but not optimized).

✅ Avatar Shop: Basic logic for purchasing and viewing items.

✅ Data Persistence: Connects to a MariaDB/MySQL database to store user, avatar, and item data (Not persists GP and event points).

## To do
- Buddy System: Add support for friend requests, friend list management, and related functionalities.

Want to help with this feature? join in!

## Known Issues
- Player levels are not displayed in the room detail view.

Have you encountered this or other bugs? Feel free to open an issue or contribute with a fix

## Technologies Used
* Java 11: Main programming language.
* Netty: Asynchronous, high-performance networking.
* Maven: Build and dependency management.
* MariaDB/MySQL: Database management for server state and player data.
* HikariCP: High-performance JDBC connection pool.
* Logback: Logging and debug framework.


## Pre requisites

Make sure you have these installed:

* JDK 11 or higher
* Apache Maven 3.8 or higher
* MariaDB 10.4 or higher (MySQL-compatible)
* GunBound: Thor's Hammer game client (GIS v376 or GBS v404)


## Environment Setup

1. Database
Create a new schema in your MariaDB/MySQL (e.g., gbth).
Import the .sql file with the necessary table structure (user, game, chest, menu, etc).

2. Properties File

Go to `src/main/resources/`.
Edit db.properties with your database credentials:

```
user=your_db_user
password=your_db_password
dburl=jdbc:mariadb://localhost:3306/gbth
useSSL=false
```

3. Server IP Configuration
Open `src/main/java/br/com/gunbound/emulator/GunBoundStarter.java`.
Change the SERVER_HOST constant to your server IP:
```
    public class GunBoundStarter {
        // ...
        private static final String SERVER_HOST = "127.0.0.1"; // Change this IP
        // ...
    }
```
- Use `0.0.0.0` to listen on all interfaces, or `127.0.0.1` for localhost/testing.

▶️ How to Run

Open a terminal in the project's root.

Compile the project:

```
    mvn clean install
```

Run the server (ensure your database is up and configured):

```
    mvn exec:java -Dexec.mainClass="br.com.gunbound.emulator.GunBoundStarter"
```

If configured correctly, you'll see Broker and Game Server startup messages in the console.

Connect using your GunBound: Thor’s Hammer game client!

## Project Structure

│ `br.com.gunbound.emulator` // Root package

├── db // Database connection management (e.g., DatabaseManager)

├── handlers // Netty handlers (GunBoundBrokerServerHandler, GunBoundGameHandler)

├── lobby // Lobby/channel management

├── model // Data entities (DTOs), DAOs, sessions

├── packets // Packet readers/writers, protocol definitions

├── room // Game room logic (GameRoom, RoomManager)

└── utils // Utilities, including encryption (GunBoundCipher)

## How to Contribute

Contributions are very welcome!

Fork this repository.

Create a new branch for your feature:
```
    git checkout -b feature/NewFeature
```

Make your changes and commit them:
```
    git commit -m 'Add NewFeature'
```

Push your branch:
```
    git push origin feature/NewFeature
```

_Open a Pull Request!_

### Contributions

[Rizzo - Lib THv404] [GunBound Thor's Hammer Lib - By Rizzo](https://github.com/samuelrizzo/gunbound-th-plugin-dll).

Thanks a lot Rizzo!

### Credits / Contributors

*This project is the result of the passion and efforts of:*

- **KyLL3R** - _Lead Developer._
- **ChoVinisTa** - _Co-Developer, Collaborator._
- **Tiddus (Gui)** - _Collaborator, Valuable support, legacy files._
- **CarlosX** - _Reference code, packet structures, and invaluable discussions (Thnx bro)._
- **Garsia** -  _Reference code and packet structure insights._
- **Jglim** (GitHub) - _Code references and documentation (thank you for your outstanding work!)._

### A Tribute to a Friend I've Lost: Special Thanks and Posthumous Message to Joabe (JoBaS).

- **JoBaS** (Bisoru_eX) - _A friend that GunBound gave me, and your friendship made a real difference in my life. You were always kind, with a heart full of goodness. Now you're in the Lord's dwelling. Thank you for everything, my friend. I'll carry you in my heart forever. I miss you!_


###### Reference Links

[JGlim - Emulator] [GunBound Thor's Hammer Emulator - By Jglim](https://github.com/jglim/gunbound-server).

[CarlosX - Broker] [GunBound WC Broker - By CarlosX](https://github.com/CarlosX/GunBoundWC).

### Screenshots

![Game Room](https://raw.githubusercontent.com/kyll3r/GunBound-Java/refs/heads/main/images/1.jpg)
![Game Play](https://raw.githubusercontent.com/kyll3r/GunBound-Java/refs/heads/main/images/2.png)

## License

This project is licensed under the MIT License. See LICENSE.txt for details.

###### Disclaimer:

The GunBound game client and all related artwork, trademarks, and assets are the exclusive property of Softnyx. This server emulator is a fan-made, non-commercial project developed solely for educational and archival purposes.