package com.queryOptimiser.restservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/relations")
public class DatabaseController {
    private Map<String, Relation> relationsMap = new HashMap<>();

//    private static final String template = "Hello, %s!";
//    private final AtomicLong counter = new AtomicLong();

    @PostMapping
    public ResponseEntity<String> relation(@RequestBody Relation relation) {
        relationsMap.put(relation.name(), relation);
        System.out.println("Relation " + relation.name() +" with " + relation.size() + " size has been successfully added!");
        return ResponseEntity.ok("Object added successfully.");
    }

    @GetMapping
    public ResponseEntity<Relation> relation(@RequestParam(value = "name") String name) {
        if (relationsMap.containsKey(name)) {
            return ResponseEntity.ok(relationsMap.get(name));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/relation")
//    public Relation relation(@RequestParam(value = "name", defaultValue = "World") String name) {
//        return new Relation(String.format(template, name), counter.incrementAndGet());
//    }
}
