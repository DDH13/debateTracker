package com.dineth.debateTracker.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getRoom() {
        return roomRepository.findAll();
    }

    public Room findRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }


}
