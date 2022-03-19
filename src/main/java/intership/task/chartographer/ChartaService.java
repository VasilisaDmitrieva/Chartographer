package intership.task.chartographer;

import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class ChartaService {
    private final ChartaRepository chartaRepository;

    public ChartaService(ChartaRepository chartaRepository) {
        this.chartaRepository = chartaRepository;
    }

    public void save(Charta charta) {
        chartaRepository.save(charta);
    }

    public Charta find(String id) {
        return chartaRepository.find(Long.parseLong(id));
    }

    public boolean setFragment(Charta charta, Fragment fragment, InputStream inputStream) {
        return chartaRepository.setFragment(charta, fragment, inputStream);
    }

    public byte[] getFragment(Charta charta, Fragment fragment) {
        return chartaRepository.getFragment(charta, fragment);
    }

    public void delete(Charta charta) {
        chartaRepository.delete(charta);
    }
}
