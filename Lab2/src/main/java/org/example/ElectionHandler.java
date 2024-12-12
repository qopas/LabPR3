package org.example;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ElectionHandler implements Runnable {

    private static final int UDP_PORT = 12345;
    private static final String MANAGER_SERVER_URL = "http://localhost:8084/update-leader";

    private String serverId;
    private DatagramSocket socket;

    public ElectionHandler(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            startElection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startElection() throws Exception {
        // Step 1: Send the election request with a random number
        Random random = new Random();
        int electionNumber = random.nextInt(1000);  // Random number to simulate election
        String message = serverId + ":" + electionNumber;

        // Broadcast election message to the UDP port
        DatagramPacket packet = new DatagramPacket(
                message.getBytes(StandardCharsets.UTF_8),
                message.length(),
                InetAddress.getByName("255.255.255.255"),
                UDP_PORT
        );
        socket.send(packet);
        System.out.println(serverId + " sent election request: " + message);

        // Step 2: Listen for the highest election number
        DatagramPacket responsePacket = new DatagramPacket(new byte[1024], 1024);
        socket.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);

        String[] parts = response.split(":");
        String respondingServerId = parts[0];
        int respondingElectionNumber = Integer.parseInt(parts[1]);

        // Step 3: Determine if this server wins or if another server wins
        if (electionNumber > respondingElectionNumber) {
            // This server is the leader, send the leader information to manager server
            System.out.println(serverId + " is the leader with election number: " + electionNumber);
            notifyManagerServer(serverId);
        } else {
            // Another server won, we should drop out and wait for new elections
            System.out.println(serverId + " dropped out. " + respondingServerId + " is the leader.");
        }
    }

    private void notifyManagerServer(String leaderId) {
        try {
            // Step 4: Notify the manager server that this server is the leader
            HttpURLConnection connection = (HttpURLConnection) new URL(MANAGER_SERVER_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write(("leader=" + leaderId).getBytes(StandardCharsets.UTF_8));
            connection.getInputStream();  // Trigger the request
            System.out.println("Manager server notified about leader: " + leaderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverId = args[0];  // e.g., Server1, Server2, or Server3
        new Thread(new ElectionHandler(serverId)).start();
    }
}

