package be.vdab.springdata.repositories;

import be.vdab.springdata.domain.Filiaal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/insertFilialen.sql")
class FiliaalRepositoryTest
        extends AbstractTransactionalJUnit4SpringContextTests {
    private static final String FILIALEN = "filialen";
    private final FiliaalRepository repository;

    public FiliaalRepositoryTest(FiliaalRepository repository) {
        this.repository = repository;
    }

    private long idVanAlfa() {
        return super.jdbcTemplate.queryForObject(
                "select id from filialen where naam = 'Alfa'", Long.class);
    }
    private long idVanBravo() {
        return super.jdbcTemplate.queryForObject(
                "select id from filialen where naam = 'Bravo'", Long.class);
    }
    @Test
    void count() {
        assertThat(repository.count()).isEqualTo(super.countRowsInTable(FILIALEN));
    }
    @Test
    void findById() {
        var optionalFliaal = repository.findById(idVanAlfa());
        assertThat(optionalFliaal.get().getNaam()).isEqualTo("Alfa");
    }
    @Test
    void findAll() {
        var filialen = repository.findAll();
        assertThat(filialen).hasSize(super.countRowsInTable(FILIALEN));
    }
    @Test
    void findAllGesorteerdOpGemeente() {
        var filialen = repository.findAll(Sort.by("gemeente"));
        assertThat(filialen).hasSize(super.countRowsInTable(FILIALEN));
        assertThat(filialen).extracting(filiaal->filiaal.getGemeente()).isSorted();
    }
    @Test
    void findAllById() {
        var idAlfa = idVanAlfa();
        var idBravo = idVanBravo();
        var filialen = repository.findAllById(Set.of(idVanAlfa(), idVanBravo()));
        assertThat(filialen).extracting(filiaal -> filiaal.getId())
                .containsOnly(idAlfa, idBravo);
    }
    @Test
    void save() {
        var filiaal = new Filiaal("Delta", "Brugge" , BigDecimal.TEN);
        repository.save(filiaal);
        var id = filiaal.getId();
        assertThat(id).isPositive();
        assertThat(super.countRowsInTableWhere(FILIALEN, "id=" + id)).isOne();
    }
    @Test
    void deleteById() {
        var id = idVanAlfa();
        repository.deleteById(id);
        repository.flush();
        assertThat(super.countRowsInTableWhere(FILIALEN, "id=" + id)).isZero();
    }
    @Test
    void deleteByOnbestaandeId() {
        assertThatExceptionOfType(EmptyResultDataAccessException.class).isThrownBy(
                () -> repository.deleteById(-1L));
    }
    @Test
    void findByGemeenteOrderByNaam() {
        var filialen = repository.findByGemeenteOrderByNaam("Brussel");
        assertThat(filialen).hasSize(2)
                .allSatisfy(
                        filiaal -> assertThat(filiaal.getGemeente()).isEqualTo("Brussel"))
                .extracting(filiaal->filiaal.getNaam()).isSorted();
    }

    @Test
    void findByOmzetGreaterThanEqual() {
        var tweeduizend = BigDecimal.valueOf(2_000);
        var filialen = repository.findByOmzetGreaterThanEqual(tweeduizend);
        assertThat(filialen).hasSize(2)
                .allSatisfy(filiaal ->
                        assertThat(filiaal.getOmzet()).isGreaterThanOrEqualTo(tweeduizend));
    }
    @Test
    void countByGemeente() {
        assertThat(repository.countByGemeente("Brussel")).isEqualTo(2);
    }
    @Test
    void findGemiddeldeOmzet() {
        assertThat(repository.findGemiddeldeOmzet()).isEqualByComparingTo("2000");
    }
    @Test
    void findMetHoogsteOmzet() {
        var filialen = repository.findMetHoogsteOmzet();
        assertThat(filialen).hasSize(1);
        assertThat(filialen.get(0).getNaam()).isEqualTo("Charly");
    }
}

