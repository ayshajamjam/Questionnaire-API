

package com.example.sweProject.controllers;

import java.util.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Need both users and questions to update & retrieve leaderboard information
import com.example.sweProject.entities.User;
import com.example.sweProject.entities.Question;
import com.example.sweProject.repositories.UserRepository;
import com.example.sweProject.repositories.QuestionRepository;

import antlr.debug.NewLineListener;

@RestController
public class UserController{
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public UserController(final UserRepository userRepository,final QuestionRepository questionRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/users")
    public Iterable<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public Optional<User> getUserById(@PathVariable("id") Integer id){
        return this.userRepository.findById(id);
    }

    @PostMapping("/users")
    public User createNewUser(@RequestBody User user){
        User newUser = this.userRepository.save(user);
        return newUser;
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable("id") Integer id, @RequestBody User updatedUser) {
        Optional<User> userToUpdateOptional = this.userRepository.findById(id);
        if (!userToUpdateOptional.isPresent()) {
            return null;
        }

        User userToUpdate = userToUpdateOptional.get();
        if (updatedUser.getName() != null) {
            userToUpdate.setName(updatedUser.getName());
        }
        if (updatedUser.getAttempted() != null) {
            userToUpdate.setAttempted(updatedUser.getAttempted());
        }
        if (updatedUser.getCorrect() != null) {
            userToUpdate.setCorrect(updatedUser.getCorrect());
        }

        User outUser  = this.userRepository.save(userToUpdate);
        return outUser;
    }

    @DeleteMapping("/users/{id}")
    public User deleteUser(@PathVariable("id") Integer id) {
        Optional<User> userToDeleteOptional = this.userRepository.findById(id);
        if (!userToDeleteOptional.isPresent()) {
            return null;
        }
        User userToDelete = userToDeleteOptional.get();
        this.userRepository.delete(userToDelete);
        return userToDelete;
    }

    // Update user to the score they gave (api: THIS user answered THIS question with THIS choice)
    @PutMapping("/users/{userid}/answer") 
    public User updateUser(@PathVariable("userid") Integer userid, @RequestBody Integer questionid, @RequestBody String choice) {
        // See if user exists
        Optional<User> userToUpdateOptional = this.userRepository.findById(userid);
        if (!userToUpdateOptional.isPresent()) {
            return null;
        }

        // See if question exists
        Optional<Question> questionOptional = this.questionRepository.findById(questionid);
        if (!questionToUpdateOptional.isPresent()) {
            return null;
        }

        // See if the choice is valid
        if(choice.length() != 1 || !(choice.charAt(0) >= 'A' && choice.charAt(0) <= 'D' || choice.charAt(0) >= 'a' && choice.charAt(0) <= 'd')){
            return null;
        }

        // At this point, a valid input is guaranteed

        User userToUpdate = userToUpdateOptional.get();
        Question questionAttempted = questionOptional.get();
        
        userToUpdate.incrementAttempted();
        if (choice.equals(questionAttempted.getAnswer())) {
            userToUpdate.incrementCorrect();
        }

        User outUser  = this.userRepository.save(userToUpdate);
        return outUser;
    }

    // Get top k users
    @GetMapping("/leaderboard/{k}")
    public List<User> getTopKUsers(@PathVariable("k") Integer k) {
        if(k == null) return null;

	    PriorityQueue<User> pq = new PriorityQueue<>((a,b) -> Double.compare(a.getPercentCorrect(),b.getPercentCorrect()));
	
        for(User user: this.getAllUsers()){
            pq.add(user);
            
            // Remove bottom tier users if size is greater than k
            if(pq.size() > k) pq.poll();
        }

        // Convert to LinkedList
        List<User> out = new LinkedList<>();
        while(!pq.isEmpty()) out.add(0,pq.poll());
        
        return out;
    }
}






