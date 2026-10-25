package gym.adapter.persistence;

import gym.domain.Invoice;
import gym.port.out.InvoiceRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileInvoiceRepository extends JsonFileRepository<Invoice, String> implements InvoiceRepository {

    public JsonFileInvoiceRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("invoices.json"), Invoice.class, Invoice::id, mapper);
    }
}
