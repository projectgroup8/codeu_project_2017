package codeu.chat.common;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Update {

    String update;

    public static final Serializer<Update> SERIALIZER = new Serializer<Update>() {

    	@Override
	    public void write(OutputStream out, Update value) throws IOException {

	      Serializers.STRING.write(out, value.update);

	    }

	    @Override
	    public Update read(InputStream in) throws IOException {

	      return new Update(
	          Serializers.STRING.read(in)
	      );

	    }
	};

    public Update(String update){
        this.update = update;
    }

    public String getUpdate(){
        return update;
    }
}
