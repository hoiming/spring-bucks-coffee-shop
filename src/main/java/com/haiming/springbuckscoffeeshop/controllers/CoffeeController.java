package com.haiming.springbuckscoffeeshop.controllers;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.services.CoffeeService;
import com.haiming.springbuckscoffeeshop.viewmodels.NewCoffeeRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.xml.ws.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coffee")
public class CoffeeController {

    @Autowired
    private CoffeeService coffeeService;

    @GetMapping(path = "/", params = "!name")
    public List<Coffee> getall(){
        return coffeeService.findAllCoffee();
    }

    @GetMapping(path= "/")
    public Coffee get(@RequestParam String name){
        Optional<Coffee> coffeeOptional = coffeeService.findOneCoffee(name);
        return coffeeOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
    }

    @PostMapping(path = "/", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Coffee addCoffeeWithoutBindingResult(@Valid NewCoffeeRequest coffee, BindingResult result){
        if(result.hasErrors()){
            System.err.println("Binding errors: " + result);
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuck you");
            throw new ValidationException("Binding errors." + result);

        }
        Coffee toSave = new Coffee();
        toSave.setPrice(coffee.getPrice());
        toSave.setName(coffee.getName());
        return coffeeService.save(toSave);
    }

    @PostMapping(path = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<Coffee> batchAddCoffee(@RequestParam("file")MultipartFile file){
        List<Coffee> coffees = new ArrayList<>();
        if(!file.isEmpty()){
            BufferedReader reader = null;
            try{
                reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String str;
                while((str = reader.readLine()) != null){
                    String[] arr = StringUtils.split(str, " ");
                    if(arr != null && arr.length == 2){
                        Coffee coffee = new Coffee();
                        coffee.setName(arr[0]);
                        coffee.setPrice(Money.of(CurrencyUnit.of("CNY"), NumberUtils.createBigDecimal(arr[1])));
                        coffee = coffeeService.save(coffee);
                        coffees.add(coffee);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return coffees;
    }

    @PostMapping(path="/reload")
    public void reloadCoffee(){
        coffeeService.reloadCoffee();
    }
}
