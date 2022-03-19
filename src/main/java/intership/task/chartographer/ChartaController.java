package intership.task.chartographer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.*;

@Controller
public class ChartaController {
    private final ChartaService chartaService;

    public ChartaController(ChartaService chartaService) {
        this.chartaService = chartaService;
    }

    @PostMapping("/chartas")
    public ResponseEntity<String> newCharta(@Valid Charta charta,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        System.out.println(charta.getWidth() + " " + charta.getHeight());
        chartaService.save(charta);
        return new ResponseEntity<>(String.valueOf(charta.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/chartas/{id}")
    public ResponseEntity<String> setFragment(@PathVariable String id,
                                      @Valid Fragment fragment,
                                      InputStream is,
                                      BindingResult bindingResult) {
        Charta charta = chartaService.find(id);
        if (charta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors() ||
            !(fragment.getX() < charta.getWidth() && fragment.getY() < charta.getHeight())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!chartaService.setFragment(charta, fragment, is)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/chartas/{id}")
    public ResponseEntity<byte[]> getFragment(@PathVariable String id,
                                              @Valid Fragment fragment,
                                              BindingResult bindingResult) {
        Charta charta = chartaService.find(id);
        if (charta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors() ||
                !(fragment.getX() < charta.getWidth() && fragment.getY() < charta.getHeight()) ||
                fragment.getWidth() > 5000 || fragment.getHeight() > 5000) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] image = chartaService.getFragment(charta, fragment);

        if (image == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @DeleteMapping("/chartas/{id}")
    public ResponseEntity<String> deleteCharta(@PathVariable String id) {
        Charta charta = chartaService.find(id);
        if (charta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        chartaService.delete(charta);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

