package com.example.fantasta.player_service.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/ping")
public class Ping 
{
    @GetMapping
    public ResponseEntity<String> ping()
    {
        return new ResponseEntity<>("Server is running", HttpStatus.OK);
    }
    
}
