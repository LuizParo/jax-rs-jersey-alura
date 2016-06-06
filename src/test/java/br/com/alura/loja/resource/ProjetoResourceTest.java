package br.com.alura.loja.resource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import br.com.alura.loja.Servidor;
import br.com.alura.loja.modelo.Projeto;

public class ProjetoResourceTest {
    private HttpServer server;
    private Client client;
    private WebTarget target;

    @Before
    public void setUp() {
        this.server = Servidor.inicializaServidor();
        this.client = ClientBuilder.newClient();
        this.target = this.client.target("http://localhost:8080");
    }
    
    @After
    public void tearDown() {
        this.server.shutdownNow();
    }

    @Test
    public void testaServidorProjeto() {
        String conteudo = this.target.path("/projetos/1").request().get(String.class);
        
        Projeto projeto = (Projeto) new XStream().fromXML(conteudo);
        
        Assert.assertNotNull(projeto);
        Assert.assertEquals("Minha loja", projeto.getNome());
    }
    
    @Test
    public void testaQueUmProjetoSejaSalvoUtilizandoPost() {
        Projeto projeto = new Projeto("Proj 1", 2000);
        
        Entity<String> entity = Entity.entity(projeto.toXML(), MediaType.APPLICATION_XML);
        Response response = this.target.path("projetos").request().post(entity);
        Assert.assertEquals(201, response.getStatus());
        
        String location = response.getHeaderString("Location");
        String projetoEmXML = this.client.target(location).request().get(String.class);
        Assert.assertTrue(projetoEmXML.contains("Proj 1"));
    }
}