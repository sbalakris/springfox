/*
 *
 *
 *
 *
 * Copyright 2015 the original author or authors.
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
 *
 *
 */

package springfox.test.contract.swaggertests

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.test.contract.swagger.SwaggerApplication
import springfox.test.contract.swagger.listeners.ObjectMapperEventListener

import static groovyx.net.http.ContentType.*

@WebAppConfiguration
@IntegrationTest("server.port:0")
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
@ContextConfiguration(
        loader = SpringApplicationContextLoader,
        classes = Config)
class ObjectMapperSanityCheck extends Specification {

  @Value('${local.server.port}')
  int port;

  def "should produce valid swagger json regardless of object mapper configuration"() {

    given: "A customized object mapper always serializing empty attributes"
      RESTClient http = new RESTClient("http://localhost:$port")

    when: "swagger json is produced"
      def response = http.get(
              path: '/v2/api-docs',
              query: [group: 'default'],
              contentType: TEXT, //Allows to access the raw response body
              headers: [Accept: 'application/json']
      )

    then: "There should not be a null schemes element"
      def slurper = new JsonSlurper()
      def swagger = slurper.parseText(response.data.text)
      !swagger.containsKey('schemes')
  }

  @Configuration
  @EnableSwagger2
  @EnableWebMvc
  @ComponentScan(basePackageClasses = [SwaggerApplication.class])
  static class Config {
    @Bean
    public Docket testCases() {
      return new Docket(DocumentationType.SWAGGER_2).select().build()
    }

    @Bean
    @Primary
    public ObjectMapper objectMapperWithIncludeAlways(){
      /* Replaces spring boots object mapper
       * //http://docs.spring.io/spring-boot/docs/current/reference/html/howto-spring-mvc.html
       */
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true )
      objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
      return objectMapper
    }

    @Bean
    public ObjectMapperEventListener objectMapperEventListener(){
      //Register an ObjectMapperConfigured event listener
      return new ObjectMapperEventListener()
    }
  }
}
