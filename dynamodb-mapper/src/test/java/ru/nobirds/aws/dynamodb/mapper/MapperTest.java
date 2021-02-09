package ru.nobirds.aws.dynamodb.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ru.nobirds.aws.dynamodb.mapper.MapperTest.Person.PersonType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class MapperTest {

    private static final String NAME = "name";
    private static final String COST = "cost";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    private static final String OWNER = "owner";
    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String ID = "id";
    private static final String ADDRESS = "address";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String TYPE = "type";
    private static final String EXTERNAL_ID = "externalId";

    private static final Pet PET = new Pet(
        1L, "test1", LocalDate.of(2000, 1, 1), 10.0,
        new Person("test2", 28L, null, PersonType.ADULT, List.of("test4")));

    private static final Map<String, AttributeValue> MAP = Map.of(
        PK, stringAttr("PET"),
        SK, stringAttr("PET#1"),
        ID, longAttr(1L),
        NAME, stringAttr("test1"),
        DATE_OF_BIRTH, stringAttr("2000-01-01"),
        COST, decimalAttr(10.0),
        OWNER, AttributeValue.builder().m(Map.of(
            NAME, stringAttr("test2"),
            PHONE_NUMBER, nullAttr(),
            TYPE, stringAttr("ADULT"),
            EXTERNAL_ID, stringAttr("C_28"),
            ADDRESS, AttributeValue.builder().l(stringAttr("test4")).build()
        )).build()
    );

    private static AttributeValue stringAttr(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private static AttributeValue nullAttr() {
        return AttributeValue.builder().nul(true).build();
    }

    private static AttributeValue longAttr(Long value) {
        return AttributeValue.builder().n(value.toString()).build();
    }

    private static AttributeValue decimalAttr(Double value) {
        return AttributeValue.builder().n(value.toString()).build();
    }

    @Test
    public void testMapFromObjectToAttributes() {
        var result = Pet.PET_MAPPER.map(PET);

        assertThat(result).containsOnlyKeys(PK, SK, ID, NAME, DATE_OF_BIRTH, COST, OWNER);
        assertThat(result.get(PK).s()).isEqualTo("PET");
        assertThat(result.get(SK).s()).isEqualTo("PET#1");
        assertThat(result.get(ID).n()).isEqualTo("1");
        assertThat(result.get(NAME).s()).isEqualTo("test1");
        assertThat(result.get(DATE_OF_BIRTH).s()).isEqualTo("2000-01-01");
        assertThat(result.get(COST).n()).isEqualTo("10.0");
        assertThat(result.get(OWNER).hasM()).isTrue();
        Map<String, AttributeValue> owner = result.get(OWNER).m();
        assertThat(owner).containsOnlyKeys(NAME, ADDRESS, TYPE, PHONE_NUMBER, EXTERNAL_ID);
        assertThat(owner.get(NAME).s()).isEqualTo("test2");
        assertThat(owner.get(EXTERNAL_ID).s()).isEqualTo("C_28");
        assertThat(owner.get(PHONE_NUMBER).nul()).isTrue();
        assertThat(owner.get(TYPE).s()).isEqualTo("ADULT");
        List<AttributeValue> address = owner.get(ADDRESS).l();
        assertThat(address).hasSize(1);
        assertThat(address.stream().map(AttributeValue::s).collect(Collectors.toList())).containsExactly("test4");
    }

    @Test
    public void testMapFromAttributesToObject() {
        var result = Pet.PET_MAPPER.map(MAP);
        assertThat(result).isEqualTo(PET);
    }

    @Test
    public void testMapFromEmptyAttributesToObject() {
        Pet pet = Pet.PET_MAPPER.map(Map.of());

        assertThat(pet.getId()).isNull();
        assertThat(pet.getName()).isNull();
        assertThat(pet.getDateOfBirth()).isNull();
        assertThat(pet.getOwner()).isNull();
    }

    @Test
    public void testMapFromEmptyObjectsToAttributes() {
        var attributes = Pet.PET_MAPPER.map(new Pet());

        assertThat(attributes).hasSize(7);
        attributes.entrySet().stream().filter(pair -> !pair.getKey().equals("pk")).map(Entry::getValue)
            .forEach(attributeValue -> assertThat(attributeValue.nul()).isTrue());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Pet {

        public static final AttributesMapper<Pet> PET_MAPPER = AttributeMappers
            .builder(Pet::new)
            .constantValue(PK, "PET", AttributeMappers.STRING)
            .number(ID, Pet::getId, Pet::setId)
            .string(NAME, Pet::getName, Pet::setName)
            .attribute(SK, Pet::getId, Pet::setId, AttributeMappers.STRING
                .map(BidirectionalMapper.hashed("PET"))
                .map(BidirectionalMapper.STRING_TO_NUMERIC))
            .decimal(COST, Pet::getCost, Pet::setCost)
            .attribute(DATE_OF_BIRTH, Pet::getDateOfBirth, Pet::setDateOfBirth,
                AttributeMappers.STRING.map(BidirectionalMapper.STRING_TO_DATE))
            .object(OWNER, Pet::getOwner, Pet::setOwner, Person.PERSON_MAPPER)
            .build();

        private Long id;
        private String name;
        private LocalDate dateOfBirth;
        private Double cost;
        private Person owner;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Person {

        private static final AttributesMapper<Person> PERSON_MAPPER = AttributeMappers.builder(
            Person::new)
            .string(NAME, Person::getName, Person::setName)
            .list(ADDRESS, Person::getAddress, Person::setAddress, AttributeMappers.STRING)
            .number(PHONE_NUMBER, Person::getPhoneNumber, Person::setPhoneNumber)
            .enumeration(TYPE, Person::getType, Person::setType, PersonType.class)
            .attribute(EXTERNAL_ID, Person::getExternalId, Person::setExternalId,
                AttributeMappers.STRING.map(BidirectionalMapper.withPrefix("C_")
                    .then(BidirectionalMapper.STRING_TO_NUMERIC)))
            .build();

        private String name;
        private Long externalId;
        private Long phoneNumber;
        private PersonType type;
        private List<String> address;

        public enum PersonType {
            ADULT, CHILD
        }

    }
}
