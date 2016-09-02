package br.edu.ifsp.btv.ads.pdmde16.flagquiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FlagQuiz extends AppCompatActivity {

    public static final String TAG = "FLAG_QUIZ";

    private final int ESCOLHAS_MENU = Menu.FIRST;
    private final int REGIOES_MENU = Menu.FIRST + 1;


    private int questaoAtual;
    private int numeroLinhas;
    private String respostaCorreta;
    private int corretas;
    private int tentativas;
    private List<String> listaArquivos; // todos os arquivos
    private List<String> listaBandeiras; // as 10 bandeiras selecionadas
    private Map<String, Boolean> regioes; // as regiões habilitadas
    private TextView questaoTextView;
    private ImageView bandeiraImageView;
    private TableLayout botoesTableLayout;
    private TextView respostaTextView;
    private Handler handler;
    private Animation chacolhar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_quiz);

        numeroLinhas = 1;
        listaArquivos = new ArrayList<>();
        listaBandeiras = new ArrayList<>();
        regioes = new HashMap<>();
        handler = new Handler();

        String[] vetor = getResources().getStringArray(R.array.listaRegioes);

        for (String regiao : vetor)
            regioes.put(regiao, true);

        questaoTextView = (TextView) findViewById(R.id.questaoTextView);
        bandeiraImageView = (ImageView) findViewById(R.id.bandeiraImageView);
        botoesTableLayout = (TableLayout) findViewById(R.id.botoesTableLayout);
        respostaTextView = (TextView) findViewById(R.id.respostaTextView);

        chacolhar = AnimationUtils.loadAnimation(this, R.anim.chacoalhar);
        reiniciar();
    }


    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, ESCOLHAS_MENU, Menu.NONE, R.string.escolhas);
        menu.add(Menu.NONE, REGIOES_MENU, Menu.NONE, R.string.regioes);
    return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case ESCOLHAS_MENU:
                final String[] listaOpcoes = getResources().getStringArray(R.array.listaOpcoes);
                AlertDialog.Builder construtor = new AlertDialog.Builder(this);
                construtor.setTitle("Nivel de Dificuldade");
                construtor.setItems(R.array.listaOpcoes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numeroLinhas = Integer.parseInt(listaOpcoes[which])/3;
                        reiniciar();
                    }
                });
                construtor.create().show();
                return true;
            case REGIOES_MENU:
                construtor = new AlertDialog.Builder(this);
                construtor.setTitle("Escolhas as Regioes");
                final String[] vetorRegioes = new String[regioes.size()];
                boolean[] selecionadas = new boolean[regioes.size()];
                int i = 0;

                for (Map.Entry<String, Boolean> entry : regioes.entrySet()) {
                    vetorRegioes[i] = entry.getKey();
                    selecionadas[i] = entry.getValue();
                    i++;
                }
                construtor.setMultiChoiceItems(vetorRegioes, selecionadas,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                regioes.put(vetorRegioes[which], isChecked);
                            }
                        }
                );

                construtor.setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reiniciar();
                            }

                        });
                    construtor.create().show();
                    return  true;
        }
        return super.onOptionsItemSelected(item);

    }



    private void reiniciar() {
        /* buscar todas as bandeiras das regiões selecionadas */
        AssetManager assets = getAssets();

        listaArquivos.clear();

        Set<String> conjuntoRegioes = regioes.keySet();

        try {
            for (String regiao : conjuntoRegioes) {

                if (regioes.get(regiao)) {

                    String[] arquivos = assets.list(regiao);

                    for (String arquivo : arquivos)
                        listaArquivos.add(arquivo.replace(".png", ""));
                }

            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao carregar as bandeiras", e);
        }

        /* zerar os contadores e as bandeiras */
        questaoAtual = 0;
        corretas = 0;
        tentativas = 0;
        listaBandeiras.clear();

        /* selecionar aleatoriamente 10 bandeiras das regiões escolhidas */
        Random aleatorio = new Random();
        int contador = 0;

        while (contador <= 10) {

            int numero = aleatorio.nextInt(listaArquivos.size());
            String arquivo = listaArquivos.get(numero);

            if (!listaBandeiras.contains(arquivo)) {
                listaBandeiras.add(arquivo);
                contador++;
            }

        }

        /* carregar a primeira bandeira (delegar) */
        carregarProximaBandeira();
    }

    private void carregarProximaBandeira() {

        /* retirar a próxima bandeira da lista de bandeiras */
        String bandeira = listaBandeiras.remove(0);
        respostaCorreta = bandeira;

        /* atualizar a interface do usuário - bandeira */
        questaoAtual++;
        questaoTextView.setText("Questão " + questaoAtual + " de 10");
        respostaTextView.setText("");

        String regiao = bandeira.substring(0, bandeira.indexOf('-'));
        String nomeArquivo = regiao + "/" + bandeira + ".png";

        AssetManager assets = getAssets();

        InputStream is = null;
        try {
            is = assets.open(nomeArquivo);

            Drawable imagem = Drawable.createFromStream(is, bandeira);
            bandeiraImageView.setImageDrawable(imagem);

        } catch (IOException e) {
            Log.e(TAG, "Erro ao abrir arquivo " + nomeArquivo, e);

        } finally {

            if (is != null) {

                try {
                    is.close();
                } catch (IOException e) {
                    // ignorar
                }

            }
        }

        /* colocar os botões de opções sem a opção correta */
        for (int linha = 0; linha < botoesTableLayout.getChildCount(); ++linha) {
            ((TableRow) botoesTableLayout.getChildAt(linha)).removeAllViews();
        }

        /* decidir aleatoriamente que botão terá a opção correta */
        /*
        1-embaralhar as bandeiras
        2-colocar a bandeira correta na ultima posiçao
        3-inflar os botoes e posiciona-los
        4-selecionar um deles aleatoriamente para ser o correto
         */
        Collections.shuffle(listaArquivos);
        listaArquivos.add(listaArquivos.remove(listaArquivos.indexOf(respostaCorreta)));

        LayoutInflater inflador = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < numeroLinhas; i ++) {

            for (int j = 0; j < 3; j++) {

                Button botao = (Button) inflador.inflate(R.layout.botao, null);
                String nome = listaArquivos.get(i * 3 + j);
                nome = getNomePais(nome);
                botao.setText(nome);
                botao.setOnClickListener(ouvidorTentativa);


                TableRow tableRow = (TableRow) botoesTableLayout.getChildAt(i);
                tableRow.addView(botao);
            }

        }

        Random aleatorio = new Random();
        int linha = aleatorio.nextInt(numeroLinhas);
        int coluna = aleatorio.nextInt(3);
        TableRow trAleatorio = (TableRow) botoesTableLayout.getChildAt(linha);
        Button botao = (Button) trAleatorio.getChildAt(coluna);
        botao.setText(getNomePais(respostaCorreta));

    }

    private String getNomePais(String bandeira) {
        return bandeira.substring(bandeira.indexOf('-')+1).replace('_', ' ');
    }

    public void enviarTentativa(Button botao) {

        /* 1- verifico se esta correta */
        String tentativa = botao.getText().toString();
        String bandeira = getNomePais(respostaCorreta);
        tentativas++;

        if (bandeira.equals(tentativa)) {

            corretas++;
            respostaTextView.setText(bandeira + "!");
            respostaTextView.setTextColor(
                    getResources().getColor(R.color.acertou));

            /* desabilitar todos os botões */
            for (int i = 0; i < botoesTableLayout.getChildCount(); i++) {
                TableRow tableRow = (TableRow) botoesTableLayout.getChildAt(i);
                for (int j = 0; j < tableRow.getChildCount(); j++) {
                    tableRow.getChildAt(j).setEnabled(false);
                }
            }

            /* 1.1- se sim, verifico se ja chegou no fim do quiz */
            if (corretas == 10) {
                /* 1.1.1 - se sim, mostro o score */

                AlertDialog.Builder construtor = new AlertDialog.Builder(this);
                construtor
                        .setTitle("Parabéns!")
                        .setMessage(String.format("%s: %.2f !",
                                "Sua pontuação", 1000 / (double) tentativas))
                        .setCancelable(true)
                        .setPositiveButton("De novo!",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        reiniciar();
                                    }
                                })
                        .create()
                        .show(); /* FLUENT INTERFACES */
            } else {
                /* 1.1.2 - se nao, carrego a proxima bandeira (com delay) */
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        carregarProximaBandeira();
                    }
                }, 1000); // crio um delay de 1 segundo
            }
        }else{
        /* 1.2- se nao (esta correta), chacoalho a bandeira */
            bandeiraImageView.startAnimation(chacolhar);
            respostaTextView.setText(R.string.erro);
            respostaTextView.setTextColor(getResources().getColor(R.color.errou));
            botao.setEnabled(false);
        }
    }
    View.OnClickListener ouvidorTentativa = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enviarTentativa((Button) v);
        }
    };
}
