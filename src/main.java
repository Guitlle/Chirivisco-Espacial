/*
 * main.java
 *
 * Created on 15 de febrero de 2009, 02:26 PM
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Random;
import java.util.*;
import java.awt.Image;
import java.awt.MediaTracker;
/**
 *
 * @author  guille
 */
class cronometro extends Thread {
    int minutos=0,segundos=0,cents=0;
    boolean nuevaTarea=false,noTareas;
    List<tarea> tareas,tareasQ;
    
    public cronometro(){
        tareas = new ArrayList<tarea>();
        tareasQ = new ArrayList<tarea>();
    }

    public void agregarTarea(tarea t){
        nuevaTarea = true;
        tareasQ.add(t);
    }
    public void ejecutarTareas(){
        Iterator iter = tareas.iterator();
        tarea t;
        while (iter.hasNext()){
            t = (tarea)iter.next();
            if(!t.activa) continue;
            t.proc();
            if(t.borrar()) { iter.remove(); continue;}
       }   
        if (nuevaTarea) {
            nuevaTarea = false;
            tareas.addAll(tareasQ);
            tareasQ.clear();   
        }
    }
}
    class tarea {
        public void proc() {}
        public boolean borrar(){ return (ciclos--<=0);}
        int ciclos = 1;
        boolean activa=true;
        public tarea(int ciclos){
            this.ciclos = ciclos;
        }
    }

class playground extends java.awt.Canvas {
    
    
    public playground (){
    }

}


class Render {
    int ancho, alto,retardo,r_iteraciones;
    Graphics2D superf;
    Image fondo;
    Capa capas[];
    Graphics2D capasG[];
    
    public Render (int ancho, int alto, Image f, Graphics2D sf, Image c[],int ret){
        fondo = f;
        superf = sf;
        capas = (Capa[]) c;
        r_iteraciones = capas.length-1;
        retardo = ret;
        capasG = new Graphics2D[capas.length];
        int i=0;
        for (Capa c2 : capas){
            capasG[i] = (Graphics2D) c2.getGraphics();
            ++i;
        }
    }
    
    boolean renderizar = false;
    private class play extends Thread {
        @Override
        public void run(){
            while(true){
                try {
                    sleep(retardo);
                    
                    if (renderizar){
                        
                        capasG[0].drawImage(fondo,0,0,null);
                        if ( r_iteraciones > 0){
                            for (int i =0 ; i<r_iteraciones; i++){
                                capasG[i+1].drawImage(capas[i],0,0,null);
                                capas[i+1].animar();
                            }
                        }  else {
                            capas[0].animar();
                            
                        }
                        superf.drawImage(capas[r_iteraciones],0,0,null);
                   }
                    
                } catch (InterruptedException ex) {

                }
            }
        }
    }
    play hilo;
    boolean inicializado = false;
    public void inicializar(){
        if (inicializado) return;
        
        inicializado = true;
        hilo = new play();
        hilo.start();
    }
    
}

class Capa extends BufferedImage {
    int ancho , alto,animsTamaño = 0;
    boolean nuevaAnim=false;
    Graphics2D graph;
    List<Animacion> anims,animsQ;
    Capa(int ancho,int alto){
        super(ancho,alto, BufferedImage.TYPE_INT_ARGB);
        graph = (Graphics2D) this.getGraphics();
        this.ancho = ancho; this.alto = alto;
        anims = new ArrayList<Animacion>();
        animsQ = new ArrayList<Animacion>();
    }
    public void borrar(){
        Composite c = graph.getComposite();
        graph.setComposite(
          AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = 
              new Rectangle2D.Double(0,0,ancho,alto);
        graph.fill(rect);
        graph.setComposite(c);
    }
    public void animar(){
        Iterator iter = anims.iterator();
        Animacion anim;
        while (iter.hasNext()){
            anim = (Animacion)iter.next();
            if(!anim.activa) continue;
            if(anim.borrar) { iter.remove(); continue;}
            if (anim.sprFlag){

                anim.dibujarSpr();
            } else if (++anim.pasoActual> (anim.pasos+1)){
               iter.remove();
               animsTamaño = anims.size();
            } else {
                anim.paso();
            }
       }   
        if (nuevaAnim) {
            nuevaAnim = false;
            anims.addAll(animsQ);
            animsQ.clear();   
        }
    }
    public void agregarAnim(Animacion a){
        nuevaAnim = true;
        animsQ.add(a);
        a.borrar = false;
        a.graph = graph;
    }
}
class Animacion {
        java.awt.Image img;
        boolean sprFlag = false;
        Graphics2D graph;
        public boolean pintarAlFinal = false,activa=true,borrar=false;
        public int pasos,pasoActual,xo,yo,dx,dy,x,y;
        
        public Animacion(int pasos, int xo,int yo, int dx, int dy,java.awt.Image spr){
            img = spr;
            this.pasos = pasos;
            this.pasoActual = -1;
            this.dx = dx; this.dy = dy;
            this.xo = xo; this.yo = yo;
        }
        public Animacion(int x,int y,String archivo){
            img = main.toolkit.createImage(main.ruta+archivo);
            this.sprFlag = true;
            this.pasos = 0;
            this.pasoActual = 0;
            this.dx = 0; this.dy = 0;
            this.xo = this.x= x; this.yo = this.y = y;
        }
        public Animacion(int x,int y,java.awt.Image spr){
            img = spr;
            this.sprFlag = true;
            this.pasos = 0;
            this.pasoActual = 0;
            this.dx = 0; this.dy = 0;
            this.xo = this.x= x; this.yo = this.y = y;
        }
        
        public void fin(){}
        public void inicio () {}
        public void pasoAlterno(){}
        public void paso(){
            if (!activa) return;
            if (pasoActual == 0){
                inicio();
                graph.drawImage(img,xo,yo,null);
                x=xo; y= yo;
            } else if (pasoActual<=pasos){
                if (dx == 0 && dy ==0) pasoAlterno();
                x= dx*pasoActual/pasos + xo; y = dy*pasoActual/pasos + yo;
                graph.drawImage(img,x,y,null);
            } else if (pasoActual == pasos+1) {
                if (pintarAlFinal) {
                    graph.drawImage(img,x,y,null);
                }
                fin();
            }
        }
     public void dibujarSpr(){
         graph.drawImage(img,x,y,null);
     }
     public void borrar(int x, int y ,int ancho,int alto){
        Composite c = graph.getComposite();
        graph.setComposite(
          AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = 
              new Rectangle2D.Double(x,y,ancho,alto);
        graph.fill(rect);
        graph.setComposite(c);
    }
}


public class main extends javax.swing.JFrame {
    static Random rnd;
    audioPB audio;
    private nombreJugador ingresoNombre;
    public int numObstaculos = 8,bombaProbabilidad = 100;
    Render canvasR,canvasLatR;
    Capa buffer,bufferLat;
    public java.awt.Image fondoPNG, fondoPNGLat,animImg;
    public java.awt.Graphics2D canvas,canvasLat;
    static public String ruta;
    static java.awt.Toolkit toolkit;
    Nave naveX;
    java.awt.Image fondoLat, fondoA,bomba,expl,gameover,portada;
    Componente bombaC,obstaculo,componentes[];
    Animacion gameoverA,completadoA;
    Componente tableroArray[][];
    public volatile boolean juego=false, pausado = false, nuevoFlag= false;
    static cronometro crono;
    File BG;
    
    /** Creates new form main */
    public main() {
        ruta =  System.getProperty("user.dir")+"/cachibaches/";
        //ruta = "/home/guille/proyectoIPC1/";
        // Objeto para generar números aleatorios
        rnd = new Random();
        // Inicializamos el audio de fondo ...
        audio = new audioPB();
        BG = new File(ruta+"BG.wav");
        audio.play(BG);
        audio.repetir= true;
        //********************************
        initComponents();
        //********************************
        // Inicializamos la ventana del nombre del jugador
        ingresoNombre = new nombreJugador();
        
        // inicializar canvas, y buffer con sus respectivos objeto graphics
        buffer = new Capa(513,573);
        fondoPNG = new Capa (513,573);
        canvas = (java.awt.Graphics2D)canvas1.getGraphics();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        // Inicializar canvas, buffer y fondo lateral
        bufferLat = new Capa(217,329);
        fondoPNGLat = new Capa (217,329);
        canvasLat = (java.awt.Graphics2D)canvas2.getGraphics();
        canvasLat.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

        // tablero
        tableroArray = new Componente[20][20];
        // cargar imágenes de fondo y sprites
        toolkit = java.awt.Toolkit.getDefaultToolkit();
        naveX = new Nave(0,0,"nave.png",20,20){
            @Override
            public void muerte(){
                activa = false;
                juego = false;
                canvasLatR.renderizar = false;
                Animacion a = new Animacion(50,x,y,0,0,expl){
                    @Override
                    public void fin(){
                        borrar=true;
                        buffer.agregarAnim(gameoverA);
                        finDeJuego();
                    }
                };
                buffer.agregarAnim(a);
            }
            @Override
            public void completado(){
                activa = false;
                juego = false;
                canvasLatR.renderizar = false;
                buffer.agregarAnim(completadoA);
                finDeJuego();
            }
        };
        
        buffer.agregarAnim(naveX);
        buffer.agregarAnim(naveX.vidaA);
        buffer.agregarAnim(naveX.vidasA);
        bufferLat.agregarAnim(naveX.panel_Lat);
        
        componentes = new Componente[24];
        // 4 motores;
        componentes[0] = new Componente(Componente.MOTOR,"motor.png" );
        componentes[1] = new Componente(Componente.MOTOR,componentes[0].img);
        componentes[2] = new Componente(Componente.MOTOR,componentes[0].img);
        componentes[3] = new Componente(Componente.MOTOR,componentes[0].img);
        componentes[4] = new Componente(Componente.MOTOR,componentes[0].img);
        // 2 turbinas
        componentes[5] = new Componente(Componente.TURBINA,"turb.png" );
        componentes[6] = new Componente(Componente.TURBINA,componentes[5].img);
        // 4 helices
        componentes[7] = new Componente(Componente.HELICE,"hel.png" );
        componentes[8] = new Componente(Componente.HELICE,componentes[7].img);
        componentes[9] = new Componente(Componente.HELICE,componentes[7].img);
        componentes[10] = new Componente(Componente.HELICE,componentes[7].img);
        // 1 copiloto
        componentes[11] = new Componente(Componente.COPILOTO,"cop.png" );
        // 4 blindajes
        componentes[12] = new Componente(Componente.BLINDAJE,"blind.png" );
        componentes[13] = new Componente(Componente.BLINDAJE,componentes[12].img);
        componentes[14] = new Componente(Componente.BLINDAJE,componentes[12].img);
        componentes[15] = new Componente(Componente.BLINDAJE,componentes[12].img);
        // 4 ruedas
        componentes[16] = new Componente(Componente.RUEDA,"ruedas.png" );
        componentes[17] = new Componente(Componente.RUEDA,componentes[16].img);
        componentes[18] = new Componente(Componente.RUEDA,componentes[16].img);
        componentes[19] = new Componente(Componente.RUEDA,componentes[16].img);
        componentes[20] = new Componente(Componente.RUEDA,componentes[16].img );
        componentes[21] = new Componente(Componente.RUEDA,componentes[16].img);
        componentes[22] = new Componente(Componente.RUEDA,componentes[16].img);
        componentes[23] = new Componente(Componente.RUEDA,componentes[16].img);
        
        bombaC = new Componente(0,bomba);
        bombaC.daño = 30;
        //Obstaculo
        obstaculo = new Componente(Componente.OBST,"obst.png");
        obstaculo.enTablero= true;
        
        bomba = toolkit.createImage(ruta+"bomba.gif");
        expl = toolkit.createImage(ruta+"explosion.gif");
        fondoA = toolkit.createImage(ruta+"fondo1.png");
        fondoLat = toolkit.createImage(ruta+"panel.png");
        gameover = toolkit.createImage(ruta+"game-over.gif");
        gameoverA = new Animacion(0,0,gameover);
        java.awt.Image completado = toolkit.createImage(ruta+"completado.gif");
        completadoA = new Animacion(0,0,completado);
        portada = toolkit.createImage(ruta+"portada.png");
        
                MediaTracker mediaTracker = new MediaTracker(canvas1);
		mediaTracker.addImage(portada, 0);
		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
			System.err.println(ie);
			System.exit(1);
		}

        fondo();
        
        // Inicializar renderizador de capas con una sola capa (buffer)
        Capa capas[] = {buffer};
        canvasR = new Render(513,573,fondoPNG,canvas,capas,50);
        canvasR.inicializar();
        // Inicializar render de una sola capa para panel lateral
        Capa capasLat[] = {bufferLat};
        canvasLatR = new Render(217,329,fondoPNGLat,canvasLat,capasLat,500);
        canvasLatR.inicializar();
        // Inicializar Cronometro
        main.crono = new cronometro() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(100);
                        ejecutarTareas();
                        if (!juego) continue;
                        this.cents += 10;
                        if (this.cents > 90){
                            this.cents = 0;
                            ++this.segundos;
                        }
                        if (this.segundos > 59){
                            this.segundos = 0;
                            ++ this.minutos;
                        }
                        String cadena = String.format("%02d:%02d:%02d", this.minutos,this.segundos,this.cents);
                        cronoLabel.setText(cadena);
                        
                        if(rnd.nextInt(bombaProbabilidad)<15) ponerBomba();
                        
                        
                    } catch (InterruptedException ex) {
                        // El cronómetro no puede interrumpirse ... asi q no hay chance de excepcion
                    }
                }
            }
        };
        crono.start();
        crono.agregarTarea(new tarea(5){
            @Override
            public void proc(){
                canvas.drawImage(portada,0,0,null);
            }
        });
    }
    
    class Componente {
        public static final int MOTOR = 1;
        public static final int TURBINA = 2;
        public static final int HELICE = 3;
        public static final int COPILOTO = 4;
        public static final int BLINDAJE = 5;
        public static final int RUEDA = 6;
        
        public static final int OBST = 7;
        
        public int daño = 0, tipo, col, fila;
        public boolean enTablero = false;
        java.awt.Image img;
        
        public Componente(int tipo, String archivo) {
                img = toolkit.createImage(ruta+archivo);
                MediaTracker mediaTracker = new MediaTracker(canvas1);
		mediaTracker.addImage(img, 0);
		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
                }
                this.tipo = tipo;
        }
        public Componente (int tipo, java.awt.Image img){
            this.tipo = tipo;
            this.img = img;
        }
        public void ponerAleatorio(){
            do {
                col = rnd.nextInt(18)+1;
                fila = rnd.nextInt(18)+1;
            } while (tableroArray[col][fila] != null);
            
            enTablero = true;
            tableroArray[col][fila] = this;
        }
        public void poner(int col, int fila){
            this.col = col; this.fila = fila;
            tableroArray[col][fila] = this;
        }
        public void dibujar(){
            if (!enTablero) return;
            Graphics2D g = (Graphics2D) fondoPNG.getGraphics();
            int xy[] = {0,0};
            xy = obtenerXY(col,fila);
            g.drawImage(img,xy[0],xy[1],null);
        }
        public void dibujarEn(int x, int y){
            Graphics2D g = (Graphics2D) fondoPNG.getGraphics();
            g.drawImage(img,x,y,null);
        }
        public void quitar(){
            tableroArray[col][fila] = null;
            enTablero = false;
            fondo();dibujarTablero();
        }
        public int puntos(){
            switch (tipo){
                case MOTOR: return 15;
                case TURBINA: return 5;
                case HELICE: return 5;
                case COPILOTO: return 25;
                case BLINDAJE: return 50;
                case RUEDA: return 15;
            }
            return 0;
        }
    }
    class Nave extends Animacion {
        int col=0,fila=0,vida=100,vidas=3, puntos=0,objetos = 0,nivel = 1;
        boolean bloq = false;
        
        Graphics2D vidaG, vidasG,panel_LatG;
        Animacion vidaA, vidasA,panel_Lat;
        int cols, filas;
        
        Nave(int x, int y, String string,int cols,int filas) {
            super(x,y,string);
            
            BufferedImage vidaI, vidasI,panel_LatI;
            
            vidaI = new BufferedImage(140,4,BufferedImage.TYPE_INT_ARGB); //346,557  486,561
            vidaG = (Graphics2D)vidaI.getGraphics();
            vidaA = new Animacion(346,557,vidaI);
            vidasI = new BufferedImage(132,43,BufferedImage.TYPE_INT_ARGB); //24,526  156,569
            vidasG = (Graphics2D)vidasI.getGraphics();
            vidasA = new Animacion(24,526,vidasI);
            
            panel_LatI = new BufferedImage(217,329,BufferedImage.TYPE_INT_ARGB); //24,526  156,569
            panel_LatG = (Graphics2D)panel_LatI.getGraphics();
            panel_Lat = new Animacion(0,0,panel_LatI);
            
            panel_LatG.setFont(new Font( "Helvetica",Font.BOLD,15 ));
            
            this.cols = cols;this.filas= filas;
            vidaG.setColor(Color.RED);
            vidasG.setColor(Color.BLACK);
        }
        public void actualizarAnims(){
            vidaG.setColor(Color.black);
            vidaG.fillRect(0, 0,139, 3);
            vidaG.setColor(Color.red);
            vidaG.fillRect(140 - vida*140/100, 0, 139, 3);
            
            panel_LatG.setBackground(new Color(0x28,0x53,0x1f));
            panel_LatG.setColor(Color.WHITE);
            panel_LatG.clearRect(32,236,60,23);
            panel_LatG.drawString(objetos+"/24", 44, 250);
            panel_LatG.clearRect(12,269,82,23);
            panel_LatG.drawString(String.format("%04d",puntos), 40, 283);
            panel_LatG.setBackground(Color.BLACK);
            panel_LatG.clearRect(106,305,34,23);
            panel_LatG.drawString("X"+vidas, 106,320);
        }
        public void dañar(int daño){
            vida -= daño;
            actualizarAnims();
            if (vida < 1){
                --vidas;
                vida = 100;
                switch(vidas){
                    case 1: 
                        vidasG.fillRect(44,0,131,42);
                        break;
                    case 2:
                        vidasG.fillRect(86,0,131,42);
                        break;
                    default:
                        vidasG.fillRect(0,0,131,42);
                        muerte();
                        return;
                }
                vidaG.fillRect(0, 0, 139, 3);
                
            }
        }
        public void completado(){}
        public void muerte(){}
        public void reiniciar(){
            reiniciarIndicadores();  
            vidas= 3;vida=100;dañar(0);puntos = 0;
            activa=true;
            objetos = 0;
        }
        public void reiniciarIndicadores(){
            Composite c = vidasG.getComposite();
            vidasG.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            Rectangle2D.Double rect = new Rectangle2D.Double(0,0,132,43); 
            vidasG.fill(rect);
            vidasG.setComposite(c);

            c = panel_LatG.getComposite();
            panel_LatG.setComposite(
                 AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            rect = new Rectangle2D.Double(0,0,200,300);
            panel_LatG.fill(rect);
            panel_LatG.setComposite(c);

                switch(vidas){
                    case 1: 
                        vidasG.fillRect(44,0,131,42);
                        break;
                    case 2:
                        vidasG.fillRect(86,0,131,42);
                        break;
                }
        }
        public boolean moverizq(){
            if (col >0 && tableroArray[col-1][fila] != obstaculo) --col;
            return true;
        }
        public boolean moverder(){
            try {
                if (col<cols && tableroArray[col+1][fila] != obstaculo) ++col;
                return true;
            } catch (java.lang.ArrayIndexOutOfBoundsException e){
                return false;
            }
        }
        public boolean moverabajo(){
            try {    
                if (fila<filas && tableroArray[col][fila+1] != obstaculo) ++fila;
                return true;
            } catch (java.lang.ArrayIndexOutOfBoundsException e){
                return false;
            }
        }
        public boolean moverarriba(){
            if (fila>0 && tableroArray[col][fila-1] != obstaculo) --fila;
            return true;
        }
        public void moverA(int X, int Y){
            this.x = X-4; this.y = Y;
        }
        public void poner(){
            int xy[] = obtenerXY(col,fila);
        
            moverA( xy[0], xy[1]); // pone nave
            if (tableroArray[col][fila] != null){
                dañar(tableroArray[col][fila].daño);
                if (tableroArray[col][fila] != bombaC){
                    ++objetos;
                    puntos += tableroArray[col][fila].puntos();
                    panel_LatG.drawImage(tableroArray[col][fila].img,rnd.nextInt(94)+55,rnd.nextInt(100)+44,null);
                    actualizarAnims();
                    tableroArray[col][fila].quitar();
                    if (objetos == 24) completado();
                }
            }
        }
    }

    
    // obtener posición en el canvas en pixeles de una posición del tablero.
    public int [] obtenerXY(float col, float fila){
        float xy[]={0,0};
        int xyret[]={0,0};
        
        xy[1] = fila*25;
        xy[0]= (float) ((col - 10) * (fila * 0.01 + 0.8))*25;
        
        xyret[1] = (int) xy[1]+4;
        xyret[0] = (int)xy[0] +256;
        return xyret;
    }
    public void fondo(){
        fondoPNGLat.getGraphics().drawImage(fondoLat,0,0,null);
        Graphics2D g = (Graphics2D) fondoPNG.getGraphics();
        g.drawImage(fondoA,0,0,null);
        float arriba=0,abajo;
        int x1,x2;
        
        g.setColor(new Color(100,130,100));
        for (int i = 0; i<21;i++){
            arriba = (20*i);
            abajo = (25*i);
            g.drawLine((int)arriba+56,4,(int)abajo+6,504);
        }
        for (int i = 0;i<21;i++){
            x1 = (5*(i*25)+4)/-50 + 56;
            x2 = 512-x1;
            arriba = i*25+4;
            g.drawLine(x1,(int)arriba,x2,(int)arriba);
        }
    }
    public void generarTablero(){
        for (int i=1;i<19;i++){
            for (int j=1; j<19;j++){
                tableroArray[j][i] = null;
            }
        }
        for (Componente c : componentes)
            c.ponerAleatorio();
        
        int xy[], col, fila;
        for ( int i = 0; i<numObstaculos;i++){
            do {
                col = rnd.nextInt(18)+1;
                fila = rnd.nextInt(18)+1;
            } while (tableroArray[col][fila] != null);
            tableroArray[col][fila] =obstaculo;
        }
    }

    public void dibujarTablero(){
        for (Componente c: componentes)
            c.dibujar();
        for (int i=1;i<19;i++){
            for (int j=1; j<19;j++){
                if (tableroArray[j][i] == obstaculo){
                    obstaculo.col = j; obstaculo.fila = i;
                    obstaculo.dibujar();
                }
            }
        }
    }
    public void ponerBomba(){
        // cierta probabilidad de que la bomba caiga por donde esta la nave...
        int tcol=rnd.nextInt(32),tfila=rnd.nextInt(32);
        if (tcol >19) tcol = naveX.col;
        if (tfila >19) tfila = naveX.fila; 
        final int col = tcol,fila = tfila;
        
        final int xy[] = obtenerXY(col,fila);
        int x = xy[0]-100, y = xy[1]-100;
        if (x < 1) x=1;
        if (y < 1) y=1;
        buffer.agregarAnim(new Animacion(5,x,y,xy[0]-x,xy[1]-y,bomba){
                    @Override
                    public void fin(){
                        if (tableroArray[col][fila] == null)  tableroArray[col][fila] = bombaC;
                        else tableroArray[col][fila].daño = 30;
                        if (naveX.col == col && naveX.fila == fila){
                            naveX.dañar(30);
                        }
                        buffer.agregarAnim(new Animacion(20,xy[0],xy[1],0,0,expl){
                            @Override
                            public void fin(){
                                if (tableroArray[col][fila] ==null) return;
                                if (tableroArray[col][fila] == bombaC) tableroArray[col][fila] = null;
                                else tableroArray[col][fila].daño =0;
                            }
                        });
                    }
                });
    }
        
        public void nuevo(){
            if (ingresoNombre.isVisible()) return;
            
            bufferLat.borrar();
            fondo();
            completadoA.borrar = true;
            gameoverA.borrar = true;
            
            dibujarTablero();
            
            juego = canvasR.renderizar = canvasLatR.renderizar =  true;
        }
        public void pausar(){
            if (juego == false) return;
            canvasR.renderizar = false;
            juego = false;
            pausado = true;
            pausar.setText("Seguir Jugando");
        }
        public void reanudar(){
            canvasR.renderizar = true;
            juego = true;
            pausado = false;
            pausar.setText("Pausa");
        }
        public void cargar(){
            try {
                BufferedReader in = new BufferedReader(new FileReader(ruta+"guardado"));
                // reiniciar la nave
                naveX.reiniciar();
                // leer nombre
                ingresoNombre.nombre.setText(in.readLine());
                // leer info del juego
                String linea = in.readLine();
                String[] valores = linea.split("_");
                System.out.print(" linea "+linea+ " valor 0 "+valores[0]);
                naveX.col = Integer.parseInt(valores[0]);
                naveX.fila = Integer.parseInt(valores[1]);
                naveX.vidas = Integer.parseInt(valores[2]);
                naveX.vida = Integer.parseInt(valores[3])+1;
                naveX.puntos = Integer.parseInt(valores[4]);
                naveX.nivel = Integer.parseInt(valores[5]);
                naveX.poner();
                bombaProbabilidad = Integer.parseInt(valores[6]);
                // leer obstaculos
                for (int i = 0; i<20; i++)
                {
                    for (int j = 0; j<20; j++){
                        if('o'== (char)in.read()) tableroArray[j][i]=obstaculo;
                        else tableroArray[j][i] =null;
                    }
                    in.readLine();
                }
                // guardar componentes
                int i =0;
                for (Componente c: componentes){
                    linea = in.readLine();
                    valores = linea.split("_");
                    c.poner(Integer.parseInt(valores[1]),Integer.parseInt(valores[2]));
                    c.enTablero = (Integer.parseInt(valores[3])==1?true:false);
                    ++i;
                    
                    if (!c.enTablero){
                        ++naveX.objetos;
                        naveX.panel_LatG.drawImage(c.img,rnd.nextInt(94)+55,rnd.nextInt(100)+44,null);
                    }
                }   
                // leer datos del cronometro
                linea = in.readLine();
                valores = linea.split("_");
                crono.minutos = Integer.parseInt(valores[0]);
                crono.segundos = Integer.parseInt(valores[1]);
                crono.cents = Integer.parseInt(valores[2]);
                in.close();
            } catch (IOException e) {
            }            
        }
        public void guardar(){
            if (!juego) return;
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(ruta+"guardado"));
                // Guardar nombre
                out.write(ingresoNombre.nombre.getText()+"\n");
                // guardar info del juego
                out.write(String.format("%02d_%02d_%d_%02d_%09d_%d_%04d\n",naveX.col,naveX.fila,naveX.vidas,naveX.vida-1,naveX.puntos,naveX.nivel,bombaProbabilidad));
                // guardar obstaculos
                for (int i = 0; i<20; i++)
                {
                    for (int j = 0; j<20; j++){
                            out.write((tableroArray[j][i]==obstaculo?"o":" "));
                    }
                    out.write("\n");
                }
                // guardar componentes
                int i =0;
                for (Componente c: componentes){
                    out.write(String.format("%02d_%02d_%02d_%d\n",i,c.col,c.fila,(c.enTablero?1:0)));
                    ++i;
                }
                // guardar cronometro
                out.write(String.format("%02d_%02d_%02d\n",crono.minutos,crono.segundos,crono.cents));
                out.close();
            } catch (IOException e) {
            }
        }
        public void records() {
           try {
                Runtime.getRuntime().exec("firefox -new-tab "+ruta+"mejoresTiempos.html");
            } catch (IOException ex) {
           }
        }
        public void finDeJuego(){
            // subir nivel
            if (naveX.objetos == 24 && naveX.vidas > 0 && naveX.nivel<6) ++naveX.nivel;
            // so todavia esta vivo, no guardar el record
            System.out.print("vidas "+naveX.vidas);
            if (naveX.vidas > 0 && naveX.nivel<5) return;
            
            // actualizar records
            try {
                int punteos[] ={0,0,0,0};
                String record = "puntos\n,\"nombre\",\"cronom\",nivel], [\n",p1,p2,p3;
                
                BufferedReader in = new BufferedReader(new FileReader(ruta+"records.js"));
                
                in.readLine();
                punteos[0] = Integer.parseInt(in.readLine());in.readLine();
                if(naveX.puntos >= punteos[0]){
                    BufferedWriter out = new BufferedWriter(new FileWriter(ruta+"records.js"));
                    out.write("Datos = [[\n");
                    record =record.replaceAll("puntos", naveX.puntos+"");
                    record =record.replaceAll("nombre", ingresoNombre.nombre.getText());
                    record =record.replaceAll("cronom", cronoLabel.getText());
                    record =record.replaceAll("nivel", (naveX.nivel)+"");
                    
                    punteos[1] = Integer.parseInt(p1=in.readLine());p1 = p1+"\n"+in.readLine()+"\n";
                    punteos[2] = Integer.parseInt(p2=in.readLine());p2 = p2+"\n"+in.readLine()+"\n";
                    punteos[3] = Integer.parseInt(p3=in.readLine());p3 = p3+"\n"+in.readLine()+"\n";
                    if (naveX.puntos >= punteos[1]){
                        if (naveX.puntos >=punteos[2]){
                            if (naveX.puntos >= punteos[3]){
                                // correr 1 a 0, 2 a 1 y 3 a 2 y poner en 3
                                out.write(p1);
                                out.write(p2);
                                out.write(p3);
                                out.write(record);
                            } else {
                                // correr 1 a 0 y 2 a 1 y poner en 2
                                out.write(p1);out.write(p2);out.write(record);out.write(p3);
                            }
                        }else {
                            // correr punteo 1 al 0 y poner punteo en 1
                            out.write(p1);out.write(record);out.write(p2);out.write(p3);
                        }
                    } else {
                        // escribir en lugar del 0
                        out.write(record);out.write(p1);out.write(p2);out.write(p3);
                    }
                    out.write("]];");

                    out.close();
                    // has quedado en los primeros 4 lugares!!!
                }
                in.close();
            } catch (IOException e){
                
            }
        }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        canvas1 = new playground();
        jPanel1 = new javax.swing.JPanel();
        cronoLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        canvas2 = new java.awt.Canvas();
        pausar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(1, 1, 1));
        setResizable(false);

        canvas1.setBackground(new java.awt.Color(0,0,0));
        canvas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                canvas1KeyPressed(evt);
            }
        });

        jPanel1.setBackground(java.awt.Color.black);
        jPanel1.setBorder(null);

        cronoLabel.setFont(new java.awt.Font("UnDotum", 1, 32));
        cronoLabel.setForeground(new java.awt.Color(84, 208, 70));
        cronoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cronoLabel.setText("00:00:00");

        jButton1.setText("Comenzar");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setText("Guardar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cargar");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Scores");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        pausar.setText("Pausa");
        pausar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pausarMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("UnDotum", 1, 18));
        jLabel1.setForeground(new java.awt.Color(77, 254, 42));
        jLabel1.setText("   ¿?");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pausar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cronoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                                .addGap(12, 12, 12)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(15, 15, 15))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(canvas2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(cronoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pausar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(canvas2, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(canvas1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void canvas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_canvas1KeyPressed
        int tecla =evt.getKeyCode();
        
        if (juego && !naveX.bloq){
            switch(tecla){
                case 37:
                    naveX.moverizq();
                    break;
                case 38:
                    naveX.moverarriba();
                    break;
                case 39:
                    naveX.moverder();
                    break;
                case 40:
                    naveX.moverabajo();
                    break;
            }
            naveX.poner();
        } else {
            switch(tecla){
                case 10:
                    // Esto es para cambiar de nivel cuando se presione enter ...
                    if (naveX.nivel == 1) break;
                        naveX.x=55;naveX.y=4;
                        naveX.col =0;
                        naveX.fila = 0;
                        naveX.activa = true;
                        naveX.objetos = 0;
                        naveX.reiniciarIndicadores();
                        switch (naveX.nivel){
                            case 2:
                                numObstaculos = 10;
                                bombaProbabilidad =90;
                                break;
                            case 3:
                                numObstaculos = 18;
                                bombaProbabilidad = 80;
                                break;
                            case 4:
                                numObstaculos = 34;
                                bombaProbabilidad = 65;
                            case 5:
                                numObstaculos = 49;
                                bombaProbabilidad = 55;
                            case 6:
                                // se queda en el nivel 5 hasta que el juego termine...
                                naveX.nivel--;
                        }
                        generarTablero();
                        nuevo();
                    
                    break;
                default:
                    // System.out.print(" tecla : "+tecla);
            }
        }
        
    }//GEN-LAST:event_canvas1KeyPressed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        if (nuevoFlag) return;
        nuevoFlag = true;
        ingresoNombre.setVisible(true);
        crono.agregarTarea(new tarea(0){
            public boolean borrar(){
                return !ingresoNombre.isVisible();
            }
            public void proc (){
                if (borrar()){
                    
                    naveX.reiniciar();
                    naveX.x=55;naveX.y=4;
                    naveX.col =0;
                    naveX.fila = 0;
                    naveX.nivel = 1;
                    crono.minutos = crono.segundos = crono.cents = 0;

                    numObstaculos = 8;
                    bombaProbabilidad = 100;

                    generarTablero();
                    nuevo();
                    
                    nuevoFlag = false;
                }
            }
        });   
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        records();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void pausarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pausarMouseClicked
        if (!pausado)    pausar();
        else reanudar();
}//GEN-LAST:event_pausarMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        guardar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        cargar();
        nuevo();
    }//GEN-LAST:event_jButton3ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new main().setVisible(true);
            }
        });
    } 
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public java.awt.Canvas canvas1;
    private java.awt.Canvas canvas2;
    private javax.swing.JLabel cronoLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton pausar;
    // End of variables declaration//GEN-END:variables
    
}
