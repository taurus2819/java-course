package io.qala.javatraining.domain;

import io.qala.datagen.junit5.BlankString;
import io.qala.datagen.junit5.Unicode;
import io.qala.datagen.junit5.seed.DatagenSeedExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static io.qala.datagen.RandomDate.after;
import static io.qala.datagen.RandomDate.beforeNow;
import static io.qala.datagen.RandomShortApi.*;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Usually we try to have 1 logical assertion per test. Otherwise tests become hard to read and they fail due
 * to multiple reasons. We violated this rule here because: a) these tests are very trivial b) otherwise they would
 * take much more space and <i>that</i> would make our tests less readable. So we're just trying to balance between
 * 2 evils.
 */
@ExtendWith(DatagenSeedExtension.class)
class DogTest {
    @Unicode(length = 1, name = "min boundary")
    @Unicode(min = 2, max = 99, name = "middle value")
    @Unicode(length = 100, name = "max boundary")
    void validName_passesValidation(String dogName, String caseName) {
        Dog dog = Dog.random().setName(dogName);
        Set<ConstraintViolation<Dog>> errors = VALIDATOR.validate(dog);
        assertEquals(errors.size(), 0, "Failed: " + caseName + ", value was: [" + dog.getName() + "].");
    }

    @BlankString(name = "size must be between 1 and 100")
    @Unicode(length = 101, name = "size must be between 1 and 100")
    void invalidName_failsValidation(String value, String expectedError) {
        Dog dog = Dog.random().setName(value);
        Set<ConstraintViolation<Dog>> errors = VALIDATOR.validate(dog);
        assertEquals(expectedError, errors.iterator().next().getMessage());
        assertEquals(1, errors.size(), "Should've been the only one: " + expectedError);
    }

    @Test void futureBirthDate_failsValidation() {
        Dog dog = Dog.random();
        assertValidationFails(dog.setTimeOfBirth(after(now()).offsetDateTime()), "must be in the past");
    }
    @Test void nullBirthDate_passesValidation() {
        Dog dog = Dog.random();
        assertBirthDateValidationPasses(dog.setTimeOfBirth(null), "null birth date");
    }
    @Test void birthDateInPast_passesValidation() {
        Dog dog = Dog.random();
        assertBirthDateValidationPasses(dog.setTimeOfBirth(beforeNow().offsetDateTime()), "birth date in the past");
    }

    @Test void positiveHeightOrWeight_passesValidation() {
        assertSizesValidationPasses(Dog.random().setHeight(Double.MIN_VALUE), "positive height");
        assertSizesValidationPasses(Dog.random().setWeight(Double.MIN_VALUE), "positive weight");
    }
    @Test void zeroHeightOrWeight_failsValidation() {
        assertValidationFails(Dog.random().setHeight(0), "must be greater than 0");
        assertValidationFails(Dog.random().setWeight(0), "must be greater than 0");
    }
    @Test void negativeHeightOrWeight_failsValidation() {
        assertValidationFails(Dog.random().setHeight(negativeDouble()), "must be greater than 0");
        assertValidationFails(Dog.random().setWeight(negativeDouble()), "must be greater than 0");
    }

    private static void assertValidationFails(Dog dog, String expectedError) {
        Set<ConstraintViolation<Dog>> errors = VALIDATOR.validate(dog);
        assertEquals(errors.iterator().next().getMessage(), expectedError);
        assertEquals(errors.size(), 1, "Should've been the only one: " + expectedError);
    }

    private static void assertBirthDateValidationPasses(Dog dog, String caseName) {
        Set<ConstraintViolation<Dog>> errors = VALIDATOR.validate(dog);
        assertEquals(errors.size(), 0, "Failed: " + caseName + ", value was: [" + dog.getTimeOfBirth() + "].");
    }
    private static void assertSizesValidationPasses(Dog dog, String caseName) {
        Set<ConstraintViolation<Dog>> errors = VALIDATOR.validate(dog);
        assertEquals(errors.size(), 0, "Failed: " + caseName + ", height was: [" + dog.getHeight() +
                "], weight was: [" + dog.getWeight() + "].");
    }

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    private static Validator VALIDATOR;
}