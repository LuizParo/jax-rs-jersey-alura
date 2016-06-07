package br.com.alura.loja.resource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import br.com.alura.loja.Servidor;
import br.com.alura.loja.modelo.Carrinho;
import br.com.alura.loja.modelo.Produto;

public class CarrinhoResourceTest {
    private HttpServer server;
    private Client client;
    private WebTarget target;

    @Before
    public void setUp() {
        this.server = Servidor.inicializaServidor();
        
        ResourceConfig config = new ResourceConfig(CarrinhoResource.class);
        config.register(LoggingFeature.class);
        
        this.client = ClientBuilder.newClient(config);
        this.target = this.client.target("http://localhost:8080");
    }
    
    @After
    public void tearDown() {
        this.server.shutdownNow();
    }

    @Test
    public void testaQueBuscarUmCarrinhoTrazOCarrinhoEsperado() {
        String conteudo = target.path("/carrinhos/1").request().get(String.class);
        Carrinho carrinho = (Carrinho) new XStream().fromXML(conteudo);
        Assert.assertEquals("Rua Vergueiro 3185, 8 andar", carrinho.getRua());
    }
    
    @Test
    public void testaQueUmCarrinhoSejaSalvoUtilizandoPost() {
        Carrinho carrinho = new Carrinho();
        carrinho.setRua("Rua Vergueiro");
        carrinho.setCidade("Sao Paulo");
        carrinho.adiciona(new Produto(314L, "Tablet", 999, 1));
        String xml = carrinho.toXML();
        
        Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);
        Response response = target.path("/carrinhos").request().post(entity);
        
        Assert.assertEquals(201, response.getStatus());
        
        String location = response.getHeaderString("Location");
        String novoCarrinhoEmXML = this.client.target(location).request().get(String.class);
        
        Assert.assertTrue(novoCarrinhoEmXML.contains("Tablet"));
    }
    
    @Test
    public void testaQueUmProdutoEhRemovidoDoCarrinho() {
        Response response = this.target.path("carrinhos/1/produtos/6237").request().delete();
        Assert.assertEquals(200, response.getStatus());
        
        String carrinhoEmXML = this.target.path("/carrinhos/1").request().get(String.class);
        Carrinho carrinho = (Carrinho) new XStream().fromXML(carrinhoEmXML);
        
        Assert.assertTrue(carrinho.getProdutos().size() == 1);
    }
}