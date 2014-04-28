import java.util.Scanner;
import java.io.* ;
import java.net.* ;

class server {

	//Largo de el arreglo donde se van a guardar los datos leidos del archivo que contiene nombres, se puede cambiar.
	final static int string_large = 30; 
	//funcion que permite volver a sus estado original el archivo que contiene la pagina que muestra la tabla con el
	//contenido para su rehuso.
	public static void refreshTableFile(){
		File getdatos_temp = new File("getdatos_temp.html");
		File getdatos_temp2 = new File("getdatos_temp2.html");
		try{
			//Manejo de archivos, tanto lectura como escritura
			FileWriter w = new FileWriter(getdatos_temp);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			FileReader r = new FileReader(getdatos_temp2);
			BufferedReader br = new BufferedReader(r);
			String line = null;
			//Lectura del archivo y luego se copia en el nuevo.
			while((line = br.readLine()) != null){
				wr.write(line);
				wr.println();
			}
			wr.close();
			bw.close();
			w.close();
			r.close();
			br.close();		
		}catch(IOException e){};	
	}
	//Funcion que permite escribir en el archivo que se utilizara como base de datos para guardar
	//nombre, puerto e ip
	public static void writeFile(String name, String port, String ip){
		File db_file = new File("db_file.txt");
		//Escritura del archivo
		try{
			//Manejo de archivos para escritura, el true se utiliza para que comienze a escribir desde el final.
			FileWriter w = new FileWriter(db_file, true);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			wr.write(name+" "+port+" "+ip);
			wr.println();
			wr.close();
			bw.close();
		}catch(IOException e){};	
	}
	//Funcion que permite modificar el archivo en donde aparecera la tabla y asi mostrarla.
	public static void writeTableFile(String[] list){
		File getdatos_temp = new File("getdatos_temp.html");		
		try{
			//Escribimos en el archivo
			FileReader r = new FileReader(getdatos_temp);
			BufferedReader br = new BufferedReader(r);
			String line = null; //Se guarda cada linea leida
			int count_tr = 0; //contador para la cantidad de <tr>
			//Lectura de cada linea hasta llegar a la tabla.
			while(br.ready()){
				line = br.readLine();
				//System.out.println(line);
				//Cuando se encuentre con el primer <tr>, comienza a escribi la tabla.
				if(line.contains("<tr>")){
					count_tr++;
					if(count_tr == 1){
						//Se cierra el archivo de lectura para su escritura
						r.close();
						br.close();
						FileWriter w = new FileWriter(getdatos_temp, true);
						//Lee el largo del archivo e imprime en el arhivo de la tabla cada linea con datos hasta que encuentra una linea nula
						for(int j = 0; j < list.length; j++){
							if(list[j] == null)
								break;
							w.write("<tr><td>"+list[j]+"</td></tr>\n");
						}
						//Finaliza el archivo html
						w.write("</tbody></table></body></html>");
						w.close();
					}
				}
			}	
		}catch(IOException e){};
	}
	//Funcion que permite leer el archivo con la informacion y devuelve un arreglo.
	public static String[] readFile(){
		//Creamos el archivo a trabajar
		File db_file = new File("db_file.txt");
		//arreglo para guardar cada linea leida
		String content[] = new String[string_large];
		try{
			//Manejo de archivos para la lectura.
			FileReader r = new FileReader(db_file);
			BufferedReader br = new BufferedReader(r);
			//Variable de tipo String para guardar cada linea leida.
			String line = null; 
			//Metodo Readline lee cada linea hasta que llega a null.
			int i = 0;
			while((line = br.readLine()) != null){
				content[i] = line;
				//System.out.println(line);
				//System.out.println(content[i]);
				i++;
			}
			if( null != r)
				r.close();
		}catch(IOException e){};
		//Retorno del string con el contenido leido.
		return content;
	}
	public static void main(String args[]) throws 
	UnknownHostException, IOException { 
	 	//Variables para el envio de los bytes para ser mostrados por el browser.
		byte[] buffer = new byte[1024]; 
		int bytes; 
		int PORT = 8000; 
		ServerSocket server = new ServerSocket(PORT); 
		System.out.println("Waiting for Client at port "+PORT);

		while(true) { 
			//El servidor espera un cliente. 
			Socket client = server.accept();
			System.out.println("Client accepted "+client);
			// nos aseguramos de que el fin de línea se ajuste al estándar 
			System.setProperty("line.separator","\r\n"); 
			//Creamos una variable de tipo Scanner para recibir la informacion del browser
			Scanner read = new Scanner (client.getInputStream()); 
			//Creamos una variable de tipo PrintWriter para enviar info al browser
			PrintWriter write = new PrintWriter(client.getOutputStream(),true); 
			// Lee el primer elemento de la cabezar del request
			String head = read.next();
			//En este punto vemos si corresponde a una request de tipo GET
				if(head.toString().equals("GET")){ 
				//Se guarda la ruta leida por el servidor y se le agrega un punto para coincidir con la ruta.
				String url = "." + read.next();
				//Si queremos leer los datos desde la base de datos
				if(url.contains("getdatos.html")){ 
					//Creamos una array de tipo string, en el cual se guarda la informacion de la base de datos, con la 
					//funcion readFile()
					String [] name_list = readFile();
					//Luego de esto, volvemos a su estado original al temporal en el cual se va a mostrar la tabla
					refreshTableFile();
					//Creamos la tabla que se va a mostrar.
					writeTableFile(name_list);
					//Guardamos la direccion de la pagina nueva que contiene la tabla
					url = "./getdatos_temp.html";
				}
				//comprobamos si existe el archivo en el servidor
				FileInputStream fis = null; 
				boolean exist = true; 
				try { 
					fis = new FileInputStream(url); 
				} catch (FileNotFoundException e) { 
					exist = false; 
				} 
				//Si el archivo existe y es mas largo que el ./ Enviamos el fichero html al browser byte por byte
				if (exist && url.length() > 2) 
					while((bytes = fis.read(buffer)) != -1 ) // enviar fichero 
						client.getOutputStream().write(buffer, 0, bytes);
				//Si no existe se manda un error mostrando que la pagina no existe 
				else {
					write.println("HTTP/1.0 404 Not Found"); 
					write.println(); 
				}
				//Cerramos el la conexion con el browser
				client.close(); 
			}
			//En el caso de que la request sea de tipo POST
			if(head.toString().equals("POST")){
				//En el caso del POST la informacion que requerimos se encuentra alfinal del request
				String line;
				String url = "." + read.next();
				try{
					//Leemos cada linea del request hasta que encuentre una linea nula
					while((line = read.nextLine()) != null){
						//Si encontra una linea vacia, esta corresponde al formato del post, luego de esta estan los datos que queremos para
						//Para realizar la consulta o guardar los datos en la base de datos
						if(line.isEmpty()){
							//Mandamos la informacion que corresponde al Action del formulario, y permitimos que el
							//Servidor abra esa pagina como un get simple.
							FileInputStream fis = null; 
							boolean exist = true; 
							try { 
								fis = new FileInputStream(url); 
							}catch (FileNotFoundException e) { 
								exist = false; 
							} 
							 
							if (exist && url.length()>2) 
								while((bytes = fis.read(buffer)) != -1 )
									client.getOutputStream().write(buffer, 0, bytes); 
							else {
								write.println("HTTP/1.0 404 Not Found"); 
							 	write.println(); 
							}
							//Finalmente se cierra la conexion al cliente, lo que permite leer la ultima linea que envia el POST.
							client.close();
						}
						//Si en esa linea encuentra el simbolo & quiere decir que trae informacion.
						if(line.contains("&")){
							//En este caso, con expresiones regulares separamos cada llave/valor y lo guardamos en un arreglo.
							String [] element = line.split("[&=]+");
							//Con la funcion writeFile, escribimos en el archivo la informacion del formulario.
							writeFile(element[1], element[3], element[5]);
							/*DEBUG DE LO QUE SE SEPARA ANTERIORMENTE
							for (int i = 0; i < elements.length; i++){
								System.out.println(elements[i]);
							}
							System.out.println(elements.length);
							for(int i = 0; i < (elements.length) ; i=i+2){
								System.out.println(elements[i]+": "+elements[i+1]);
							}*/
						}
					}
				}catch(Exception e){};
			}
		}
	}
}

