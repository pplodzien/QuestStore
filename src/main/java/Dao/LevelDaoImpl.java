package Dao;

import Model.Classroom;
import Model.Level;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LevelDaoImpl implements LevelDao {



    public List<Level> getLevels() {
        List<Level> levels = new ArrayList<>();
        try(Connection con = C3P0DataSource.getInstance().getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM \"Levels\"")){

            while (rs.next()) {
                int levelNumber = rs.getInt("id");
                int experienceNeeded = rs.getInt("experience");
                Level level = new Level.Builder().withLevelNumber(levelNumber).withExperienceNeeded(experienceNeeded).build();
                levels.add(level);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return levels;
        }



    public void editLevels(List<Level> levels) {
        try(Connection con = C3P0DataSource.getInstance().getConnection()){
            for(Level level: levels){
                int levelNumber = level.getLevelNumber();
                int experienceNeeded = level.getExperienceNeeded();
                PreparedStatement stmt = null;
                stmt = con.prepareStatement("UPDATE \"Levels\" SET experience = ? WHERE id = ?");
                stmt.setInt(1, experienceNeeded);
                stmt.setInt(2, levelNumber);
                stmt.executeUpdate();
                stmt.close();
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }




}
