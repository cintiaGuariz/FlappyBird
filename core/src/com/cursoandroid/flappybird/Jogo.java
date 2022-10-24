package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {
	//Atributos de texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	//Atributos de configurações
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoInicialHorizontalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMax = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;

	//Formas para colisão
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoBaixo;
	private Rectangle retanguloCanoCima;

	//Exibição de textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//Configurações dos sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Objeto salvar pontuação
	Preferences preferencias;

	//Objeto para a câmera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {

		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {

		//Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	/*
	0 - Jogo inicial, pássaro parado
	1 - Começa o jogo
	2 - Colidiu
	 */
	private void verificarEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0){
			//Aplica evento de toque na tela
			if (toqueTela){
				gravidade = -15;
				somVoando.play();
				estadoJogo = 1;
			}

		}else if (estadoJogo == 1){
			//Aplica evento de toque na tela
			if (toqueTela){
				gravidade = -15;
				somVoando.play();
			}

			//Movimentar o cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 250;
			if (posicaoCanoHorizontal < -canoBaixo.getWidth()){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}

			if (pontos >= 5){
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 350;
			}

			if (pontos >= 10){
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 350;
			}

			if (pontos >= 15){
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 450;
			}

			if (pontos >= 20){
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 550;
			}

			//Aplicar gravidade no pássaro
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if (estadoJogo == 2){

			if (pontos > pontuacaoMax){
				pontuacaoMax = pontos;
				preferencias.putInteger("pontuacaoMax", pontuacaoMax);

				preferencias.flush();
			}

			/*Aplicar gravidade no pássaro
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;*/

			posicaoInicialHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if (toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoInicialHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	private void detectarColisoes(){

		circuloPassaro.set(50 + posicaoInicialHorizontalPassaro + passaros[0].getWidth()/2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2);

		retanguloCanoCima.set(posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());

		retanguloCanoBaixo.set(posicaoCanoHorizontal, 0 - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());

		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);

		if (colidiuCanoBaixo || colidiuCanoCima){
			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}

		/*
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		shapeRenderer.setColor(Color.RED);

		shapeRenderer.circle(50 + passaros[0].getWidth()/2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2);

		shapeRenderer.rect(posicaoCanoHorizontal, 0 - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());

		shapeRenderer.rect(posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());

		shapeRenderer.end();*/
	}

	private void desenharTexturas(){

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo, 0,0, larguraDispositivo, alturaDispositivo);

		batch.draw(passaros[(int) variacao], 50 + posicaoInicialHorizontalPassaro, posicaoInicialVerticalPassaro);

		batch.draw(canoBaixo, posicaoCanoHorizontal, 0 - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical);

		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2 - 10, alturaDispositivo - 100);

		if (estadoJogo == 2){

			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo/2 - 200,
					alturaDispositivo/2 - gameOver.getHeight()/2);
			textoMelhorPontuacao.draw(batch, "Seu recorde é: " + pontuacaoMax + " pontos", larguraDispositivo/2 - 230,
					alturaDispositivo/2 - gameOver.getHeight());
		}

		batch.end();
	}

	private void inicializarTexturas(){

		batch = new SpriteBatch();

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");

		gameOver = new Texture("game_over.png");
	}

	private void inicializarObjetos(){

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 700;
		random = new Random();

		//Configurações de textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().scale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().scale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().scale(2);

		//Formas geométricas para colisões
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		//Inicializar sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//Configura preferências dos objetos
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMax = preferencias.getInteger("pontuacaoMax", 0);

		//Configuração da câmera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	private void validarPontos(){
		if (posicaoCanoHorizontal < 50 - passaros[0].getWidth()){//Passou da posição do pássaro
			if (!passouCano){
				pontos++;
				somPontuacao.play();
				passouCano = true;
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;
		//Verifica variação para bater asas do pássaro
		if (variacao > 3)
			variacao = 0;
	}
	
	@Override
	public void dispose () {

	}
}
