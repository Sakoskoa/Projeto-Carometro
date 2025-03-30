package view;

import model.DAO;
import utils.Validador;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.Date;
import java.sql.Connection;

public class Carometro extends JFrame {

    private JPanel panel1;
    private JPanel footerPanel;
    private JLabel lblData;
    private JTextField txtRA;
    private JTextField txtNome;
    private JLabel lblFoto;
    private JButton btnCarregarFoto;
    private JButton btnAdicionar;

    DAO dao = new DAO();
    private Connection con;
    private PreparedStatement pst;

    //instanciar objeto para o fluxo de bytes
    private FileInputStream fis;

    //variável global para armazenar o tamanho da imagem(bytes)
    private int tamanho;

    // Definindo o painel panel1
    public Carometro() {
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                super.windowActivated(e);
                status();  // Atualiza o status da conexão com o banco
                setarData();  // Atualiza a data no rodapé
            }
        });

        setTitle("Carômetro");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/instagram.png")));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 360);

        // Painel principal com layout absoluto
        panel1 = new JPanel();
        setContentPane(panel1);
        panel1.setLayout(null);  // Usando Layout absoluto

        // Criando o JLabel e JTextField para RA
        JLabel lblRA = new JLabel("RA:");
        lblRA.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRA.setBounds(20, 30, 100, 20);  // Posição do RA (x, y, largura, altura)

        txtRA = new JTextField(10); // Tamanho do campo de texto (em número de colunas)
        txtRA.setFont(new Font("Arial", Font.PLAIN, 12));
        txtRA.setBounds(50, 25, 130, 30); // Posição do campo de texto RA (x, y, largura, altura)
        txtRA.setDocument(new Validador(6));
        txtRA.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String caracteres = "0123456789";
                if (!caracteres.contains(e.getKeyChar() + "")) {
                    e.consume();
                }
            }
        });

        // Criando o JLabel e JTextField para Nome
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNome.setBounds(20, 86, 100, 20);  // Posição do Nome (x, y, largura, altura)

        txtNome = new JTextField(10);
        txtNome.setFont(new Font("Arial", Font.PLAIN, 12));
        txtNome.setBounds(65, 80, 280, 30);  // Posição do campo de texto Nome (x, y, largura, altura)
        txtNome.setDocument(new Validador(30));

        // Adicionando os campos ao painel principal
        panel1.add(lblRA);
        panel1.add(txtRA);
        panel1.add(lblNome);
        panel1.add(txtNome);

        // Criando o JLabel para a foto e carregando a imagem
        lblFoto = new JLabel();
        lblFoto.setBounds(380, 14, 256, 256);  // Posição e tamanho exato da foto (x, y, largura, altura)
        lblFoto.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        panel1.add(lblFoto);  // Adicionando o lblFoto ao painel principal

        // Carregando a imagem e ajustando ao JLabel
        ImageIcon fotoIcon = new ImageIcon(getClass().getResource("/img/camera.png"));
        Image fotoImage = fotoIcon.getImage();
        Image fotoRedimensionada = fotoImage.getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_SMOOTH);  // Redimensionando a imagem para o tamanho do JLabel
        lblFoto.setIcon(new ImageIcon(fotoRedimensionada));  // Definindo a imagem no JLabel

        // Criando o botão Carregar Imagem
        btnCarregarFoto = new JButton("Carregar Foto");
        btnCarregarFoto.setBounds(204, 130, 140, 23);
        btnCarregarFoto.setForeground(new Color(20,100,250));
        btnCarregarFoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarFoto();
            }
        });

        //Criando o botão de adicionar imagem
        btnAdicionar = new JButton();
        ImageIcon btnIcon = new ImageIcon(getClass().getResource("/img/create.png"));
        btnAdicionar.setIcon(btnIcon);
        btnAdicionar.setBounds(20,195,64,64);
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionar();
            }
        });

        panel1.add(btnCarregarFoto);
        panel1.add(btnAdicionar);

        // Criando o rodapé
        footerPanel = new JPanel();
        footerPanel.setBackground(new Color(20, 100, 250)); // Cor de fundo do rodapé
        footerPanel.setLayout(new BorderLayout()); // Usando BorderLayout para garantir que o rodapé ocupe toda a largura

        // Criando o JLabel para a data
        lblData = new JLabel("", SwingConstants.CENTER);  // Inicializando o JLabel com texto vazio
        lblData.setFont(new Font("Arial", Font.PLAIN, 12));
        lblData.setForeground(Color.WHITE);

        // Adicionando uma margem ao redor do JLabel para afastar da borda
        lblData.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // Afastando 20px da margem esquerda e direita

        // Criando o ícone e colocando à direita
        JPanel iconPanel = new JPanel(); // Painel para o ícone à direita
        iconPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 10)); // Alinhando à direita e ajustando o espaçamento
        iconPanel.setOpaque(false); // Tornando o painel do ícone transparente

        // A lógica do status já define o ícone, então apenas adicionamos o ícone conforme a conexão
        ImageIcon footerIcon = null;
        JLabel iconLabel = null;
        try {
            con = dao.conectar();
            if (con == null) {
                // Banco de dados desconectado
                footerIcon = new ImageIcon(getClass().getResource("/img/dboff.png"));
            } else {
                // Banco de dados conectado
                footerIcon = new ImageIcon(getClass().getResource("/img/dbon.png"));
            }
            iconLabel = new JLabel(footerIcon); // Criando o JLabel para o ícone

            // Adicionando o ícone ao painel de ícones
            iconPanel.add(iconLabel);
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        // Adicionando o texto e a data no centro e o ícone à direita
        JPanel dataPanel = new JPanel();  // Adicionando um painel para garantir que a data tenha espaço suficiente
        dataPanel.setLayout(new FlowLayout(FlowLayout.LEFT));  // Alinhando à esquerda
        dataPanel.setOpaque(false);  // Transparente
        dataPanel.add(lblData);  // Adicionando o JLabel da data

        footerPanel.add(dataPanel, BorderLayout.WEST);  // Adicionando o painel de data à esquerda
        footerPanel.add(iconPanel, BorderLayout.EAST); // Ícone na parte direita

        // Posicionando o rodapé na parte inferior da janela (usando setBounds)
        footerPanel.setBounds(0, 272, 700, 50); // Definindo a posição do rodapé

        // Adicionando o painel do rodapé no JFrame, com BorderLayout.SOUTH
        panel1.add(footerPanel);  // Adicionando o rodapé no painel principal

        // Chamar o método setarData ao iniciar para já mostrar a data
        setarData();
    }

    public static void main(String[] args) {
        Carometro carometro = new Carometro();
        carometro.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void status() {
        Connection con = null;
        try {
            con = dao.conectar();
            JLabel iconLabel = null;
            ImageIcon footerIcon = null;
            if (con == null) {
                // Banco de dados desconectado
                footerIcon = new ImageIcon(getClass().getResource("/img/dboff.png"));
            } else {
                // Banco de dados conectado
                footerIcon = new ImageIcon(getClass().getResource("/img/dbon.png"));
            }
            iconLabel = new JLabel(footerIcon);

            // Atualizando o painel com o ícone correto
            footerPanel.removeAll();
            footerPanel.add(iconLabel, BorderLayout.EAST); // Adicionando ícone à direita
            footerPanel.revalidate();
            footerPanel.repaint();

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void setarData() {
        Date data = new Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);
        String dataFormatada = formatador.format(data);
        lblData.setText(dataFormatada);  // Atualizando o JLabel com a data atual
    }

    private void carregarFoto(){
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Selecionar arquivo");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo de imagens(*.PNG,*.JPG,*.JPEG)","png","jpg","jpeg"));
        int resultado = jfc.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION){
            try {
                fis = new FileInputStream(jfc.getSelectedFile());
                tamanho = (int) jfc.getSelectedFile().length();
                Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblFoto.getWidth(),lblFoto.getHeight(),Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(foto));
                lblFoto.updateUI();
            } catch (Exception e){
                System.out.println(e);
            }
        }
    }

    private void adicionar(){
        String insert = "insert into alunos(nome,foto) values(?,?)";
        try {
            con = dao.conectar();
            pst = con.prepareStatement(insert);
            pst.setString(1,txtNome.getText());
            pst.setBlob(2,fis,tamanho);
            int confirma = pst.executeUpdate();
            if (confirma == 1){
                JOptionPane.showMessageDialog(null,"Aluno cadastrado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null,"Erro! Aluno não cadastrado.");
            }
            con.close();
    } catch (Exception e){
            System.out.println(e);
        }
    }

}
