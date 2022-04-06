 package exercice4;

// Utilisation de la classe Message :
// Message m = new Message("type","val");
// String json = Message.toJson(m);
// Message m2 = Message.fromJson(json);

public class Message {

        private String type = null;
        private String mess = null;

        public Message () {
        }
       
        public Message(String type, String mess) {
                this.type = type;
                this.mess = mess;
        }
       
        public String toString() {
        	return ("[type : "+type+", mess : "+mess+"]");
        }

        public String getType() {
                return type;
        }

        public void setType(String type) {
                this.type = type;
        }

        public String getMess() {
                return mess;
        }

        public void setMess(String mess) {
                this.mess = mess;
        }
}
