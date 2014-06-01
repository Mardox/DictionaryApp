package com.thirthydaylabs.dictionaryapp.app;

/**
 * Created by HooMan on 25/05/2014.
 */
public class Word {

    String _word;
    String _definition;
    String _type;

    public Word(){

    }

    public Word(String word, String definition, String type){
        this._word = word;
        this._definition = definition;
        this._type = type;
    }


    public String getWord(){
        return this._word;
    }

    public void setWord(String word){
        this._word = word;
    }

    public String getDefinition(){
        return this._definition;
    }

    public void setDefinition(String definition){
        this._definition = definition;
    }

    public String getType(){
        return this._type;
    }

    public void setType(String type){
        this._type = type;
    }

}
