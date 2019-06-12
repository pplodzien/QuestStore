package Controller;

import Dao.ClassroomDao;
import Dao.DaoException;
import Dao.MentorDao;
import Dao.StudentDao;
import Model.Classroom;
import Model.Mentor;
import Model.Student;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class AdminHandleMentors implements HttpHandler {
    private MentorDao mentorDao;
    private ClassroomDao classroomDao;
    private StudentDao studentDao;
    private SessionHandler sessionHandler;

    public AdminHandleMentors(MentorDao mentorDao, ClassroomDao classroomDao, StudentDao studentDao, SessionHandler sessionHandler){
        this.mentorDao = mentorDao;
        this.studentDao = studentDao;
        this.classroomDao = classroomDao;
        this.sessionHandler = sessionHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        if (method.equals("GET")){
            try {
                getPage(httpExchange);
            } catch (DaoException e) {
                e.printStackTrace();
            }
        }
        if (method.equals("POST")) {
            Map<String, String> inputs = getFormData(httpExchange);

            if(inputs.get("formType").equals("editMentor")){
                editMentor(inputs);
            }

            else if(inputs.get("formType").equals("addMentor")){
                addMentor(inputs);
                try {
                    getPage(httpExchange);
                } catch (DaoException e) {
                    e.printStackTrace();
                }
            }
            else if (inputs.get("formType").equals("deleteMentor")) {
                try {
                    deleteMentor(inputs);
                    getPage(httpExchange);
                } catch (DaoException e) {
                    e.printStackTrace();
                }

            }
            else if(inputs.get("formType").equals("logout")){
                try {
                    sessionHandler.deleteSession(httpExchange);
                    getLoginPage(httpExchange);
                } catch (DaoException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    private void addMentor(Map<String, String> inputs){
        String firstName = inputs.get("firstName");
        String lastName = inputs.get("lastName");
        String email = inputs.get("email");
        String password = inputs.get("password");
        try{
            mentorDao.addMentor(firstName, lastName, email, password);
        }
        catch (DaoException e){
            e.printStackTrace();
        }
    }



    private void editMentor(Map<String, String> inputs){
        String firstName = inputs.get("firstName");
        String lastName = inputs.get("lastName");
        String email = inputs.get("email");
        int id = Integer.valueOf(inputs.get("mentorId"));
        System.out.println(firstName);
        System.out.println(id);
        try {
            mentorDao.editMentor(id, firstName, lastName, email);
        } catch (DaoException e) {
            e.printStackTrace();
        }
    }

    private void deleteMentor(Map<String, String> inputs) throws DaoException {
        mentorDao.deleteMentor(Integer.valueOf(inputs.get("deleteMentorId")));
        classroomDao.setMentorIdAsNull(Integer.valueOf(inputs.get("deleteMentorId")));
    }



    private void getPage(HttpExchange httpExchange) throws IOException, DaoException {
        SessionHandler sessionHandler = new SessionHandler();
        Optional<HttpCookie> cookie = sessionHandler.getSessionCookie(httpExchange);

        try{
            int userId = sessionHandler.getUserId(httpExchange, cookie);
            List<Mentor> mentors = mentorDao.getMentors();
            List<Classroom> classrooms = classroomDao.getClassrooms();
            List<Student> students = studentDao.getAllStudents();
            JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/admin_mentors.twig");
            JtwigModel model = JtwigModel.newModel();
            model.with("mentors", mentors);
            model.with("classrooms", classrooms);
            model.with("students", students);
            String response = template.render(model);
            sendResponse(httpExchange, response);
        }

        catch (DaoException | NoSuchElementException e){
            getLoginPage(httpExchange);
        }

    }

    private void getLoginPage(HttpExchange httpExchange) throws IOException{
        httpExchange.getResponseHeaders().set("Location", "/login");
        httpExchange.sendResponseHeaders(302,0);
    }


    private void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private Map<String, String> getFormData(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();
        Map<String, String> inputs = LoginController.parseFormData(formData);
        return inputs;
    }
}
