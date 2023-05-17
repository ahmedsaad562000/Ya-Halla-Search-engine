package Backend;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public String getEmployeesById(@PathVariable String id) {
        return "ID: " + id;
    }


    // make a post request to /api/employees/{id}


    @RestController
    public static class MyController {

        @PostMapping("/upload")
        public HashMap<String, Double> upload(
                             @RequestParam("search") String username , @RequestParam("phrase") boolean phrase) {
            HashMap<String, Double> map = new HashMap<String, Double>();
            map.put("link1", 1.0);
            map.put("link2", 5.5);
            map.put("link3", 9.9);
            map.put("time", 0.0005);
            return map;
        }

    }

}