package com.tested.quizapp.service;

import com.tested.quizapp.dao.QuestionDao;
import com.tested.quizapp.dao.QuizDao;
import com.tested.quizapp.model.Question;
import com.tested.quizapp.model.QuestionWrapper;
import com.tested.quizapp.model.Quiz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tested.quizapp.model.Response;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    QuizDao quizDao;
    @Autowired
    QuestionDao questionDao;

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    public ResponseEntity<String> createQuiz(String category, int numQ, String title){
        List<Question> questions = questionDao.findRandomQuestionsByCategory(category, numQ);
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestions(questions);
        quizDao.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(Integer id){
        Optional<Quiz> quiz = quizDao.findById(id);
        List<Question> questionsFromDB = quiz.get().getQuestions();
        List<QuestionWrapper> questionsForUser = new ArrayList<>();
        for (Question q : questionsFromDB){
            QuestionWrapper qw = new QuestionWrapper(q.getId(), q.getQuestionTitle(), q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4());
            questionsForUser.add(qw);
        }

        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {
        Optional<Quiz> optionalQuiz = quizDao.findById(id);
        if (optionalQuiz.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Quiz quiz = optionalQuiz.get();
        List<Question> questions = quiz.getQuestions();
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correct = 0;

        for (Response response : responses) {
            Question question = questionMap.get(response.getId());
            if (question != null && response.getResponse().equals(question.getCorrectAnswer())) {
                correct++;
            }
        }

        return new ResponseEntity<>(correct, HttpStatus.OK);
    }
}
