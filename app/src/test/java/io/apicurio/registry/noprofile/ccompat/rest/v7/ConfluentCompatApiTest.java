/*
 * Copyright 2022 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.noprofile.ccompat.rest.v7;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.AbstractResourceTestBase;
import io.apicurio.registry.ccompat.dto.CompatibilityLevelDto;
import io.apicurio.registry.ccompat.dto.CompatibilityLevelParamDto;
import io.apicurio.registry.ccompat.dto.SchemaContent;
import io.apicurio.registry.ccompat.rest.ContentTypes;
import io.apicurio.registry.rest.v2.beans.UpdateState;
import io.apicurio.registry.rest.v2.beans.VersionSearchResults;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.utils.tests.TestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.response.ValidatableResponse;
import jakarta.enterprise.inject.Typed;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.apicurio.registry.noprofile.ccompat.rest.CCompatTestConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the REST API exposed at endpoint "/ccompat/v7" follows the
 * <a href="https://docs.confluent.io/7.2.1/schema-registry/develop/api.html">Confluent API specification</a>,
 * unless otherwise stated.
 *
 * @author Carles Arnal
 */
@QuarkusTest
@Typed(ConfluentCompatApiTest.class)
public class ConfluentCompatApiTest extends AbstractResourceTestBase {

    public static final String BASE_PATH = "/ccompat/v7";

    @NotNull
    public String getBasePath() {
        return BASE_PATH;
    }

    /**
     * Endpoint: /subjects/(string: subject)/versions
     */
    @Test
    public void testCreateSubject() throws Exception {
        final String SUBJECT = "subject1";
        // POST
        ValidatableResponse res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_SIMPLE_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
        /*int id = */
        res.extract().jsonPath().getInt("id");

        // Verify
        given()
                .when()
                .get(getBasePath() + "/subjects/").then().body(Matchers.containsString("subject1"));
    }

    /**
     * Endpoint: /subjects/(string: subject)/versions
     */
    @Test
    public void testDefaultQuoted() throws Exception {
        final String SUBJECT = "subject1";
        // POST
        ValidatableResponse res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_SIMPLE_WRAPPED_WITH_DEFAULT_QUOTED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));

        int id = res.extract().jsonPath().getInt("id");

        // Verify
        given()
                .when()
                .get(getBasePath() + "/schemas/ids/{id}", id)
                .then()
                .statusCode(200)
                .body(Matchers.containsString("EloquaContactRecordData"));
    }

    @Test
    public void testJsonDefault() throws Exception {
        final String SUBJECT = "subject1";
        // POST
        ValidatableResponse res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_SIMPLE_WRAPPED_WITH_JSON_DEFAULT)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));

        int id = res.extract().jsonPath().getInt("id");

        // Verify
        given()
                .when()
                .get(getBasePath() + "/schemas/ids/{id}", id)
                .then()
                .statusCode(200)
                .body(Matchers.containsString("prefix4.public.my_table"));
    }

    /**
     * Endpoint: /subjects/(string: subject)/versions
     */
    @Test
    public void testCreateDuplicateContent() throws Exception {
        final String SUBJECT = "testCreateDuplicateContent";
        // POST content1
        ValidatableResponse res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
        int id1 = res.extract().jsonPath().getInt("id");

        // POST content2
        res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_2_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
        int id2 = res.extract().jsonPath().getInt("id");

        // POST content3 (duplicate of content1)
        res = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
        int id3 = res.extract().jsonPath().getInt("id");

        // ID1 and ID3 should be the same because they are the same content within the same subject.
        Assertions.assertEquals(id1, id3);
    }

    /**
     * Endpoint: /compatibility/subjects/{subject}/versions/{version}
     */
    @Test
    public void testCompatibilityCheck() throws Exception {
        final String SUBJECT = "testCompatibilityCheck";
        // Prepare
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_2_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body(anything());

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CONFIG_BACKWARD)
                .put(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .body(anything());

        // POST
        TestUtils.retry(() -> {
            given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .body(SCHEMA_1_WRAPPED)
                    .post(getBasePath() + "/compatibility/subjects/{subject}/versions/{version}", SUBJECT, "latest")
                    .then()
                    .statusCode(200)
                    .body("is_compatible", equalTo(true));
            given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .body(SCHEMA_SIMPLE_WRAPPED)
                    .post(getBasePath() + "/compatibility/subjects/{subject}/versions/{version}", SUBJECT, "latest")
                    .then()
                    .statusCode(200)
                    .body("is_compatible", equalTo(false));
        });
    }

    /**
     * Endpoint: /compatibility/subjects/{subject}/versions/{version}
     */
    @Test
    public void testCompatibilityInvalidSchema() throws Exception {

        final String SUBJECT = "testCompatibilityInvalidSchema";

        // Prepare
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(VALID_AVRO_SCHEMA)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body(anything());



        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CONFIG_BACKWARD)
                .put(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .body(anything());

        // POST
        TestUtils.retry(() -> {
            given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .body(SCHEMA_INVALID_WRAPPED)
                    .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                    .then()
                    .statusCode(422)
                    .body("error_code", Matchers.allOf(Matchers.isA(Integer.class), Matchers.equalTo(42201)));
        });
    }

    @Test
    public void testDisabledStateCheck() throws Exception {
        final String SUBJECT = "testDisabledStateCheck";
        // Prepare
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_SIMPLE_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));

        //Update state
        UpdateState updateState = new UpdateState();
        updateState.setState(ArtifactState.DISABLED);
        given()
                .when()
                .contentType(ContentTypes.JSON)
                .body(updateState)
                .put("/registry/v2/groups/default/artifacts/{artifactId}/state", SUBJECT)
                .then()
                .statusCode(204);


        // GET - shouldn't return as the state has been changed to DISABLED
        TestUtils.retry(() -> {
            given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .get(getBasePath() + "/subjects/{subject}/versions/{version}", SUBJECT, "latest")
                    .then()
                    .statusCode(404)
                    .body("error_code", Matchers.allOf(Matchers.isA(Integer.class), Matchers.equalTo(40401)));
        });

        // GET schema only - shouldn't return as the state has been changed to DISABLED
        TestUtils.retry(() -> {
            given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .get(getBasePath() + "/subjects/{subject}/versions/{version}/schema", SUBJECT, "latest")
                    .then()
                    .statusCode(404)
                    .body("error_code", Matchers.allOf(Matchers.isA(Integer.class), Matchers.equalTo(40401)));
        });
    }

    @Test
    public void testSchemaTypes() throws Exception {
        //verify
        String[] types = given()
                .when()
                .get(getBasePath() + "/schemas/types")
                .then()
                .statusCode(200)
                .extract().as(String[].class);

        assertEquals(3, types.length);
        assertEquals("JSON", types[0]);
        assertEquals("PROTOBUF", types[1]);
        assertEquals("AVRO", types[2]);
    }

    /**
     * Endpoint: /subjects/{subject}/versions
     */
    @Test
    public void testRegisterWithType() throws Exception {
        final String SUBJECT = "subjectRegisterWithType";

        // POST
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_SIMPLE_WRAPPED_WITH_TYPE)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
    }


    /**
     * Endpoint: /schemas/ids/{int: id}
     */
    @Test
    public void testGetSchemaById() throws Exception {
        //VERIFY AVRO, no schema type should be returned
        registerSchemaAndVerify(SCHEMA_SIMPLE_WRAPPED, "subject_test_avro", null);
        //VERIFY JSON, JSON must be returned as schemaType
        registerSchemaAndVerify(JSON_SCHEMA_SIMPLE_WRAPPED_WITH_TYPE, "subject_test_json", "JSON");
        //VERIFY PROTOBUF, PROTOBUF must be returned as schemaType
        registerSchemaAndVerify(PROTOBUF_SCHEMA_SIMPLE_WRAPPED_WITH_TYPE, "subject_test_proto", "PROTOBUF");
    }

    private void registerSchemaAndVerify(String schema, String subject, String schemaTye) throws Exception {
        registerSchemaInSubject(schema, subject);

        final Integer avroSchemaGlobalId = given().when().get(getBasePath() + "/subjects/{subject}/versions/latest", subject).body().jsonPath().get("id");
        verifySchemaType(avroSchemaGlobalId, schemaTye);
    }

    private void registerSchemaInSubject(String schema, String subject) {
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(schema)
                .post(getBasePath() + "/subjects/{subject}/versions", subject)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)));
    }

    private void verifySchemaType(long globalId, String schemaType) {
        //Verify
        Assertions.assertEquals(given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/schemas/ids/{id}", globalId)
                .then()
                .extract()
                .body().jsonPath().get("schemaType"), schemaType);
    }

    /**
     * Endpoint: /schemas/ids/{int: id}/versions
     */
    @Test
    public void testGetSchemaVersions() throws Exception {
        final String SUBJECT = "subjectTestSchemaVersions";
        final String SECOND_SUBJECT = "secondSubjectTestSchemaVersions";

        //Create two versions of the same artifact
        // POST
        final Integer contentId1 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");
        Assertions.assertNotNull(contentId1);



        //Register different schema in second subject
        // POST
        final Integer contentId2 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_2_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SECOND_SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");

        Assertions.assertNotNull(contentId2);

        //Register again schema 1 in different subject gives same contentId
        // POST
        final Integer contentId3 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SECOND_SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");

        Assertions.assertNotNull(contentId3);

        TestUtils.retry(() -> {
            //Verify
            final ResponseBodyExtractionOptions body = given()
                    .when()
                    .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                    .get(getBasePath() + "/schemas/ids/{id}/versions", contentId1)
                    .then()
                    .statusCode(200)
                    .extract().body();

            assertEquals(contentId1, contentId3);

            final List<String> subjects = body.jsonPath().get("subject");
            final List<Integer> versions = body.jsonPath().get("version");
            Assertions.assertTrue(List.of("subjectTestSchemaVersions", "secondSubjectTestSchemaVersions").containsAll(subjects));
            Assertions.assertTrue(List.of(1, 2).containsAll(versions));
        });
    }

    /**
     * Endpoint: /ccompat/v7/subjects/{subject}/versions/{version}/referencedby
     */
    @Test
    public void testGetSchemaReferencedVersions() throws Exception {
        final String SUBJECT_1 = "testGetSchemaReferencedVersions1";
        final String SUBJECT_2 = "testGetSchemaReferencedVersions2";

        // Create first artifact
        // POST
        final Integer contentId1 = given().when().contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED).post(getBasePath() + "/subjects/{subject}/versions", SUBJECT_1).then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");
        Assertions.assertNotNull(contentId1);

        // Create artifact that references the first one
        // POST
        System.out.printf((SCHEMA_3_WRAPPED_TEMPLATE) + "%n", SUBJECT_1, 1);
        final Integer contentId2 = given().when().contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(String.format(SCHEMA_3_WRAPPED_TEMPLATE, SUBJECT_1, 1)).post(getBasePath() + "/subjects/{subject}/versions", SUBJECT_2).then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(1)))
                .extract().body().jsonPath().get("id");
        Assertions.assertNotNull(contentId2);

        //Verify
        Integer[] versions = given().when()
                .get(getBasePath() + "/subjects/{subject}/versions/{version}/referencedby", SUBJECT_1, 1L).then().statusCode(200)
                .extract().as(Integer[].class);

        assertEquals(1, versions.length);
        assertTrue(Arrays.asList(versions).contains(contentId2));
    }

    /**
     * Endpoint: /ccompat/v7/config PUT
     * Endpoint: /ccompat/v7/config GET
     */
    @Test
    public void testConfigEndpoints() throws Exception {

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CONFIG_BACKWARD)
                .put(getBasePath() + "/config/")
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelDto.class);

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/config/")
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelParamDto.class);
    }

    @Test
    public void validateAcceptHeaders() throws Exception {

        //Test application/json
        String[] types = given()
                .when()
                .accept(ContentTypes.JSON)
                .get(getBasePath() + "/schemas/types")
                .then()
                .statusCode(200)
                .extract().as(String[].class);

        assertTypes(types);

        //Test application/vnd.schemaregistry.v1+jso
        types = given()
                .when()
                .accept(ContentTypes.COMPAT_SCHEMA_REGISTRY_V1)
                .get(getBasePath() + "/schemas/types")
                .then()
                .statusCode(200)
                .extract().as(String[].class);

        assertTypes(types);

        //Test application/vnd.schemaregistry+json
        types = given()
                .when()
                .accept(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/schemas/types")
                .then()
                .statusCode(200)
                .extract().as(String[].class);

        assertTypes(types);
    }

    public void assertTypes(String[] types) {

        assertEquals(3, types.length);
        assertEquals("JSON", types[0]);
        assertEquals("PROTOBUF", types[1]);
        assertEquals("AVRO", types[2]);
    }

    /**
     * Endpoint: /schemas/ids/{int: id}/versions
     */
    @Test
    public void testMinifiedSchema() throws Exception {
        final String SUBJECT = "testMinifiedSchema";
        String testSchemaExpanded = resourceToString("../avro-expanded.avsc");

        ObjectMapper objectMapper = new ObjectMapper();
        SchemaContent schemaContent = new SchemaContent(testSchemaExpanded);

        // POST
        final Integer contentId1 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(schemaContent))
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");

        Assertions.assertNotNull(contentId1);

        String minifiedSchema = resourceToString("../avro-minified.avsc");
        SchemaContent minifiedSchemaContent = new SchemaContent(minifiedSchema);

        //Without the canonical hash mode, this will fail with a 404
        //With the profile
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(minifiedSchemaContent))
                .post(getBasePath() + "/subjects/{subject}", SUBJECT)
                .then()
                .statusCode(404)
                .body("error_code", Matchers.allOf(Matchers.isA(Integer.class), Matchers.equalTo(40403)));


        // POST
        //Create returns a different id, wrong behaviour with the canonical mode enabled since the schema is the same.
        Assertions.assertNotEquals(contentId1, given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(minifiedSchemaContent))
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id"));
    }

    @Test
    public void testSubjectConfigEndpoints() throws Exception {
        final String SUBJECT = "subject2";
        // Prepare
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_2_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body(anything());

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CONFIG_BACKWARD)
                .put(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelDto.class);

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CONFIG_BACKWARD)
                .get(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelParamDto.class);

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .delete(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelParamDto.class);

        final CompatibilityLevelParamDto compatibilityLevel = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .delete(getBasePath() + "/config/{subject}", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(CompatibilityLevelParamDto.class);

        assertEquals(compatibilityLevel.getCompatibilityLevel(), CompatibilityLevelDto.Level.NONE.name());
    }

    @Test
    public void testDeleteSchemaVersion() throws Exception {
        final String SUBJECT = "testDeleteSchemaVersion";
        final Integer contentId1 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_1_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");
        Assertions.assertNotNull(contentId1);

        final Integer contentId2 = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(SCHEMA_2_WRAPPED)
                .post(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");

        Assertions.assertNotNull(contentId2);

        //check versions list
        var versionsConfluent = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(Integer[].class);

        assertEquals(2, versionsConfluent.length);

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/subjects/{subject}/versions/{version}", SUBJECT, "1")
                .then()
                .statusCode(200);
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/subjects/{subject}/versions/{version}", SUBJECT, "2")
                .then()
                .statusCode(200);


        var versionsApicurio = given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(VersionSearchResults.class)
                .getVersions();

        assertEquals(2, versionsApicurio.size());

        versionsConfluent = given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(Integer[].class);

        assertEquals(2, versionsConfluent.length);

        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .get(getBasePath() + "/subjects/{subject}/versions/{version}", SUBJECT, "1")
                .then()
                .statusCode(200);

        versionsApicurio = given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .extract().as(VersionSearchResults.class)
                .getVersions();

        assertEquals(2, versionsApicurio.size());

        given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}/versions/1", SUBJECT)
                .then()
                .statusCode(200);
        given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}/versions/1/meta", SUBJECT)
                .then()
                .statusCode(200);
        given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}/meta", SUBJECT)
                .then()
                .statusCode(200);
        given()
                .when()
                .get("/registry/v2/groups/default/artifacts/{subject}", SUBJECT)
                .then()
                .statusCode(200);
    }

    /**
     * This is the same test case as in
     *
     * @see io.apicurio.registry.ccompat.rest.CCompatCanonicalModeTest
     * in v7, there is no need for the configuration option since the param is available at the API level.
     */
    @Test
    public void normalizedSchemasTest() throws Exception {
        final String SUBJECT = "testSchemaExpanded";
        String testSchemaExpanded = resourceToString("../avro-expanded.avsc");

        ObjectMapper objectMapper = new ObjectMapper();
        SchemaContent schemaContent = new SchemaContent(testSchemaExpanded);

        // POST
        // creating the content normalized so it can be found later
        final Integer contentId1 = given()
                .when()
                .queryParam("normalize", true)
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(schemaContent))
                .post(V7_BASE_PATH + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.greaterThanOrEqualTo(0)))
                .extract().body().jsonPath().get("id");

        Assertions.assertNotNull(contentId1);

        SchemaContent minifiedSchemaContent = new SchemaContent(resourceToString("../avro-minified.avsc"));

        //without normalize the content, getting the schema by content does not work
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(minifiedSchemaContent))
                .post(V7_BASE_PATH + "/subjects/{subject}", SUBJECT)
                .then()
                .statusCode(404);

        //normalizing the content, getting the schema by content works
        given()
                .when()
                .queryParam("normalize", true)
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(minifiedSchemaContent))
                .post(V7_BASE_PATH + "/subjects/{subject}", SUBJECT)
                .then()
                .statusCode(200);

        // POST
        //Create just returns the id from the existing schema, since the canonical hash is the same.
        Assertions.assertEquals(contentId1, given()
                .when()
                .queryParam("normalize", true)
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(objectMapper.writeValueAsString(minifiedSchemaContent))
                .post(V7_BASE_PATH + "/subjects/{subject}/versions", SUBJECT)
                .then()
                .statusCode(200)
                .body("id", Matchers.allOf(Matchers.isA(Integer.class), Matchers.equalTo(contentId1)))
                .extract().body().jsonPath().get("id"));
    }
}
