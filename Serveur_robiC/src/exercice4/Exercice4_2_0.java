package exercice4;

 import java.awt.Color;

/*
	(space add robi (rect.class new)) (robi translate 130 50) (robi setColor yellow) (space add momo (oval.class new)) (momo setColor red) (momo translate 80 80) (space add pif (image.class new alien.gif)) (pif translate 100 0) (space add hello (label.class new "Hello world")) (hello translate 10 10) (hello setColor black)
(space add robi (rect.class new))
(robi translate 130 50)
(robi setColor yellow)
(space add momo (oval.class new))
(momo setColor red)
(momo translate 80 80)
(space add pif (image.class new alien.gif))
(pif translate 100 0)
(space add hello (label.class new "Hello world"))
(hello translate 10 10)
(hello setColor black)

*/


import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import graphicLayer.*;
import stree.parser.*;
import tools.Tools;




public class Exercice4_2_0 {
	// Une seule variable d'instance
	Environment environment = new Environment();
	int port = 8000;
	Socket socket;
	ServerSocket serverSocket ;
	int nbC = 0;
	
	public Exercice4_2_0() {
		GSpace space = new GSpace("Exercice 4", new Dimension(200, 100));
		space.open();

		Reference spaceRef = new Reference(space);
		Reference rectClassRef = new Reference(GRect.class);
		Reference ovalClassRef = new Reference(GOval.class);
		Reference imageClassRef = new Reference(GImage.class);
		Reference stringClassRef = new Reference(GString.class);

		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("sleep", new Sleep());

		spaceRef.addCommand("add", new AddElement());
		spaceRef.addCommand("del", new DelElement());
		
		rectClassRef.addCommand("new", new NewElement());
		ovalClassRef.addCommand("new", new NewElement());
		imageClassRef.addCommand("new", new NewImage());
		stringClassRef.addCommand("new", new NewString());

		environment.addReference("space", spaceRef);
		environment.addReference("rect.class", rectClassRef);
		environment.addReference("oval.class", ovalClassRef);
		environment.addReference("image.class", imageClassRef);
		environment.addReference("label.class", stringClassRef);
		
		this.mainLoop();
	}
	
	private void mainLoop() {
		//while (true) {
			try {
				serverSocket = new ServerSocket(port);
				
				while(true) {
					nbC++;
					System.out.println("Attente d'un client <"+nbC+"> dans le port "+port);
					socket = serverSocket.accept();
					System.out.println("Client <"+nbC+"> Accepter ");
					
					Traitement traitement = new Traitement(socket,nbC);
					traitement.start();
					
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			//ON vas l'utiliser après
			// lecture d'une serie de s-expressions au clavier (return = fin de la serie)
			/*String input = Tools.readKeyboard(); 
			// creation du parser
			SParser<SNode> parser = new SParser<>();
			// compilation
			List<SNode> compiled = null;
			try {
				compiled = parser.parse(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// execution des s-expressions compilees
			Iterator<SNode> itor = compiled.iterator();
			while (itor.hasNext()) {
				new Interpreter().compute(environment, itor.next());
			}*/
		//}
	}
	
	public class Traitement extends Thread{
		private Socket socket = null;
		private int id;
		private BufferedReader br = null;
		private PrintStream ps = null;
		

		Traitement(Socket s,int x){
			try {
				this.socket = s;	
				this.id = x;
	    		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
				ps = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }

		public void run() {
			String json,cmd;
			Message msg;
			while(true){
				
				try {
					json = br.readLine();
					msg = (Message)JSON.Json2Java(json, Message.class);
					
					if(msg.getType().equals("script")) {
						cmd = msg.getMess();
						System.out.println("Client["+id+"]>> commande : "+cmd);
						
						// creation du parser
						SParser<SNode> parser = new SParser<>();
						// compilation
						List<SNode> compiled = null;
						compiled = parser.parse(cmd);
						// execution des s-expressions compilees
						Iterator<SNode> itor = compiled.iterator();
						
						int i =0;
						while (itor.hasNext()) {
							SNode snode = itor.next();
							//System.out.println("SNode ===> "+snode.toString());
							new Interpreter().compute(environment,snode );
							i++;
							ps.println("1 Script <"+i+"> bien executé");
						}
						ps.println("0 ");
					
					}else if(msg.getType().equals("cmd")){
						
						if(msg.getMess().equals("bye")) {
							break;
						}
					}else {
						System.out.println("C est pas un script !!");
					}
				} catch (SocketException e) {
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				socket.close();
				br.close();
				ps.close();
				System.out.println("Client["+id+"]>> Quitté.");
			} catch (IOException e) {
			}
			
			
			
		}
		
	}// FIN CLASS Traitement **************************************************

	public interface Command {
		// le receiver est l'objet qui va executer method
		// method est la s-expression resultat de la compilation
		// du code source a executer
		// exemple de code source : "(space setColor black)"
		abstract public Reference run(Reference receiver, SNode method);
	}// FIN INTERFACE Command **************************************************

	public class Reference {
		Object receiver;
		Map<String, Command> primitives;

		public Reference(Object receiver) {

			this.receiver = receiver;
			primitives = new HashMap<String, Command>();
		}

		Command getCommandByName(String selector) {
			Command c;
			c = primitives.get(selector);
			return c;
		}

		public void addCommand(String selector, Command primitive) {
			primitives.put(selector, primitive);
		}
		

		public Reference run(SNode expr) {
			Command cmd;
			cmd = primitives.get(expr.get(1).contents());
			//try {
			
			return cmd.run(this, expr);
			/*}catch(Exception e) {
				System.err.println("Error : la s_expression n'est pas cohérante !!");
			}*/
		}

		public Object getReceiver() {
			return this.receiver;
		}
	}// FIN CLASS Reference **************************************************

	public class Environment {
		HashMap<String, Reference> variables;

		public Environment() {
			variables = new HashMap<String, Reference>();
		}

		public void addReference(String name, Reference ref) {
			variables.put(name, ref);
		}
		
		public void removeReference(String name) {
			Reference ref = getReferenceByName(name);
			ref.primitives.clear();			
			variables.remove(name);
			System.out.println("removeObject ok");
		}

		Reference getReferenceByName(String name) {
			Reference ref;
			ref = variables.get(name);

			return ref;

		}
	}// FIN CLASS Environment **************************************************
	
	
	public class SetColor implements Command {
		public Reference run(Reference ref, SNode espr) {
			String couleur = espr.get(2).contents().toString();
			Color c = Tools.getColorByName(couleur);
			//System.out.println("++++SetColor - "+ref.getReceiver().getClass().getName());
			
			if("graphicLayer.GSpace".equals(ref.getReceiver().getClass().getName())) {
				((GSpace) ref.getReceiver()).setColor(c);
			}else if("graphicLayer.GString".equals(ref.getReceiver().getClass().getName())){
				((GString) ref.getReceiver()).setColor(c);
			}else {
				((GElement) ref.getReceiver()).setColor(c);
			}
			

			return ref;

		}
	}// FIN CLASS SetColor **************************************************
	
	public class Sleep implements Command {
		public Reference run(Reference ref, SNode espr) {
			int res1 = Integer.parseInt(espr.get(2).contents());

			// Thread.sleep(res1);
			Tools.sleep(res1);

			return ref;

		}
	}// FIN CLASS Sleep **************************************************
	
	public class Translate implements Command {
		public Reference run(Reference ref, SNode espr) {
			int res1 = Integer.parseInt(espr.get(2).contents());
			int res2 = Integer.parseInt(espr.get(3).contents());
			((GElement) ref.receiver).translate(new Point(res1, res2));

			return ref;

		}
	}// FIN CLASS Translate **************************************************
	
	public class SetDim implements Command {
		public Reference run(Reference ref, SNode espr) {
			int res1 = Integer.parseInt(espr.get(2).contents());
			int res2 = Integer.parseInt(espr.get(3).contents());
			
			//System.out.println("++++setDim++ "+ref.getReceiver().getClass().getName());
			
			
			((GBounded) ref.getReceiver()).setDimension(new Dimension(res1, res2));

			return ref;

		}
	}// FIN CLASS SetDim **************************************************
	
	class NewElement implements Command {
		public Reference run(Reference reference, SNode method) {
			try {
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GElement>) reference.getReceiver()).getDeclaredConstructor().newInstance();
				Reference ref = new Reference(e);
				ref.addCommand("setColor", new SetColor());
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				return ref;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}// FIN CLASS NewElement **************************************************
	
	class NewImage implements Command {
		public Reference run(Reference reference, SNode method) {
						
			String cmd1 = method.get(2).contents();
			//System.out.println("cmd1=" + cmd1);
			File path = new File(cmd1);
			BufferedImage rawImage = null;
			try {
				rawImage = ImageIO.read(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			GImage image = new GImage(rawImage);
			Reference refe = new Reference(image);

			refe.addCommand("translate", new Translate());

			return refe;
			
		}
	}// FIN CLASS NewImage **************************************************
	
	class NewString implements Command {
		public Reference run(Reference reference, SNode method) {
			/*try {
				@SuppressWarnings("unchecked")
				GString e = new GString("+++test-str+++");
				e.setColor(Color.red);
				e.setPosition(new Point(20,10));
				e.setFont(new Font("Arial",Font.BOLD, 18));
				
				Reference ref = new Reference(e);
				ref.addCommand("setColor", new SetColor());
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				return ref;
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			
			String cmd1 = method.get(2).contents();
			GString chaine = new GString(cmd1);
			Reference refe = new Reference(chaine);
			refe.addCommand("translate", new Translate());
			refe.addCommand("setColor", new SetColor());
			return refe;
		}
	}// FIN CLASS NewString **************************************************
	
	class AddElement implements Command {
		public Reference run(Reference reference, SNode method) {
			GContainer recever = (GContainer) reference.getReceiver();
			//GRect robiX = new GRect();

			//System.out.println("******refNewElm******");
			Reference refNewElm = new Interpreter().compute(environment, method.get(3));
			
			environment.addReference(method.get(2).contents().toString(), refNewElm);
			GElement obj = (GElement) refNewElm.getReceiver();
			

			recever.addElement(obj);
			
			recever.repaint();
			return refNewElm;
		}
	}// FIN CLASS AddElement **************************************************
	
	class DelElement implements Command {
		public Reference run(Reference reference, SNode method) {
			Interpreter inter = new Interpreter();
			Reference refe = environment.getReferenceByName(method.get(2).contents());

			Reference receveur = environment.getReferenceByName(method.get(0).contents());
			((GContainer) receveur.getReceiver()).removeElement((GElement) refe.getReceiver());
			environment.removeReference(method.get(2).contents().toString());
			((GContainer) receveur.getReceiver()).repaint();
			return null;
		}
	}// FIN CLASS DelElement **************************************************
	
	class Interpreter {
		public Reference compute(Environment env, SNode exp ){
			Reference refElm = env.getReferenceByName(exp.get(0).contents().toString());

			
			return refElm.run(exp);
			
		}
	}// FIN CLASS Interpreter **************************************************
	
	
	
	
	
	
	public static void main(String[] args) {
		new Exercice4_2_0();
	}

}