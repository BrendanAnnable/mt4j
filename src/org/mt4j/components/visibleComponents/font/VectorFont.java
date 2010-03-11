/***********************************************************************
 * mt4j Copyright (c) 2008 - 2009, C.Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.components.visibleComponents.font;

import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.font.fontFactories.IFontFactory;
import org.mt4j.components.visibleComponents.font.fontFactories.TTFontFactory;
import org.mt4j.util.MTColor;

import processing.core.PApplet;


/**
 * A vector font.
 * 
 * @author Christopher Ruff
 */
public class VectorFont implements IFont {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(VectorFont.class.getName());
	static{
//		logger.setLevel(Level.ERROR);
//		logger.setLevel(Level.WARN);
		logger.setLevel(Level.DEBUG);
		SimpleLayout l = new SimpleLayout();
		ConsoleAppender ca = new ConsoleAppender(l);
		logger.addAppender(ca);
	}
	
	/** The characters. */
	private VectorFontCharacter[] characters;
	
	/** The default horizontal adv x. */
	private int defaultHorizontalAdvX;
	
	/** The font family. */
	private String fontFamily;
	
	/** The font id. */
	private String fontId;
	
	/** The font max ascent. */
	private int fontMaxAscent;
	
	/** The font max descent. */
	private int fontMaxDescent;
	
	/** The units per em. */
	private int unitsPerEM;
	
	/** The font file name. */
	private String fontFileName;
	
	/** The original font size. */
	private int originalFontSize;
	
	/** The uni code to char. */
	private HashMap<String , VectorFontCharacter> uniCodeToChar;
	
	/** The char name to char. */
	private HashMap<String , VectorFontCharacter> charNameToChar;

	/** The fill color. */
	private MTColor fillColor;
	
	/** The stroke color. */
	private MTColor strokeColor;
	
//	/**
//	 * The Constructor.
//	 * 
//	 * @param characters2 the characters2
//	 */
//	public VectorFont(VectorFontCharacter[] characters2){
//		this(new VectorFontCharacter[0], 500, "", 800, -200, 1000, 100);
//	}
	
	
//	/**
//	 * The Constructor.
//	 * 
//	 * @param characters the characters
//	 * @param defaultHorizontalAdvX the default horizontal adv x
//	 * @param fontFamily the font family
//	 * @param fontMaxAscent the font max ascent
//	 * @param fontMaxDescent the font max descent
//	 * @param originalFontSize the original font size
//	 */
//	public VectorFont(VectorFontCharacter[] characters, int defaultHorizontalAdvX, String fontFamily, int fontMaxAscent, int fontMaxDescent, int originalFontSize) {
//		this(characters, defaultHorizontalAdvX, fontFamily, fontMaxAscent, fontMaxDescent, 1000, originalFontSize);
//	}

	/**
 * The Constructor.
 * 
 * @param characters the characters
 * @param defaultHorizontalAdvX the default horizontal adv x
 * @param fontFamily the font family
 * @param fontMaxAscent the font max ascent
 * @param fontMaxDescent the font max descent
 * @param unitsPerEm the units per em
 * @param originalFontSize the original font size
 * @param fillColor the fill color
 * @param strokeColor the stroke color
 */
	public VectorFont(VectorFontCharacter[] characters, int defaultHorizontalAdvX, String fontFamily, int fontMaxAscent, int fontMaxDescent, int unitsPerEm, int originalFontSize,
			MTColor fillColor,
			MTColor strokeColor
	) {
		super();
		this.characters = characters;
		this.defaultHorizontalAdvX = defaultHorizontalAdvX;
		this.fontFamily = fontFamily;
		this.originalFontSize = originalFontSize;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		
//		this.fontSize = fontSize;
//		this.fontUnitsPerEm = fontUnitsPerEm;
		
		this.fontId = "";
		
		this.fontMaxAscent 	= fontMaxAscent;
		this.fontMaxDescent = fontMaxDescent;
		
		this.unitsPerEM = unitsPerEm;
		
		//Put characters in hashmaps for quick access
		uniCodeToChar 	= new HashMap<String, VectorFontCharacter>();
		charNameToChar 	= new HashMap<String, VectorFontCharacter>();
		
		for (int i = 0; i < characters.length; i++) {
			VectorFontCharacter currentChar = characters[i];
			uniCodeToChar.put(currentChar.getUnicode(), currentChar);
			charNameToChar.put(currentChar.getName(), currentChar);
		}
	}
	

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontCharacterByName(java.lang.String)
	 */
	public IFontCharacter getFontCharacterByName(String characterName){
		VectorFontCharacter returnChar = charNameToChar.get(characterName);
		if (returnChar == null){
			logger.warn("Font couldnt load charactername: " + characterName);
		}
		return returnChar;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontCharacterByUnicode(java.lang.String)
	 */
	public IFontCharacter getFontCharacterByUnicode(String unicode){
		VectorFontCharacter returnChar = uniCodeToChar.get(unicode);
		if (returnChar == null){
			logger.warn("Font couldnt load characterunicode: " + unicode);
			
			//This is a kind of hacky way to try to dynamically load characters from a .ttf
			//font that were not loaded by default. 
			if (fontFileName != null && fontFileName.length() > 0 && fontFileName.endsWith(".ttf")){
				IFontFactory fontFactory = FontManager.getInstance().getFactoryForFileSuffix(".ttf");
				if (fontFactory != null && fontFactory instanceof TTFontFactory){
					TTFontFactory ttFontFactory = (TTFontFactory)fontFactory;
					if (this.getCharacters().length > 0 && this.getCharacters()[0] != null && this.getCharacters()[0] instanceof MTComponent){
						MTComponent comp = (MTComponent)this.getCharacters()[0];
						PApplet pa = comp.getRenderer();
						VectorFontCharacter[] characters = ttFontFactory.getTTFCharacters(pa, unicode, fillColor, strokeColor, this.fontFileName, this.originalFontSize);
						if (characters.length == 1 && characters[0] != null){
							VectorFontCharacter loadedCharacter = characters[0];
							VectorFontCharacter[] newArray = new VectorFontCharacter[this.getCharacters().length + 1];
							System.arraycopy(this.getCharacters(), 0, newArray, 0, this.getCharacters().length);
							newArray[newArray.length-1] = loadedCharacter;
							this.setCharacters(newArray);
							returnChar = loadedCharacter;
							logger.debug("Re-loaded missing character: " + unicode + " from the font: " + this.fontFileName);
						}	 
					}
				}
			}
		}
		return returnChar;
	}

	
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getCharacters()
	 */
	public IFontCharacter[] getCharacters() {
		return characters;
	}

	/**
	 * Sets the characters.
	 * 
	 * @param characters the new characters
	 */
	public void setCharacters(VectorFontCharacter[] characters) {
		uniCodeToChar.clear();
		charNameToChar.clear();
		for (int i = 0; i < characters.length; i++) {
			VectorFontCharacter currentChar = characters[i];
			uniCodeToChar.put(currentChar.getUnicode(), currentChar);
			charNameToChar.put(currentChar.getName(), currentChar);
		}
		this.characters = characters;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getDefaultHorizontalAdvX()
	 */
	public int getDefaultHorizontalAdvX() {
		return defaultHorizontalAdvX;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontFamily()
	 */
	public String getFontFamily() {
		return fontFamily;
	}

	/**
	 * Sets the font family.
	 * 
	 * @param fontFamily the new font family
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontMaxAscent()
	 */
	public int getFontMaxAscent() {
		return fontMaxAscent;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontMaxDescent()
	 */
	public int getFontMaxDescent() {
		return fontMaxDescent;
	}

	/**
	 * Sets the font max ascent.
	 * 
	 * @param fontMaxAscent the new font max ascent
	 */
	public void setFontMaxAscent(int fontMaxAscent) {
		this.fontMaxAscent = fontMaxAscent;
	}

	/**
	 * Sets the font max descent.
	 * 
	 * @param fontMaxDescent the new font max descent
	 */
	public void setFontMaxDescent(int fontMaxDescent) {
		this.fontMaxDescent = fontMaxDescent;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontAbsoluteHeight()
	 */
	public int getFontAbsoluteHeight(){
		return ((Math.abs(fontMaxAscent)) + (Math.abs(fontMaxDescent)));
	}
	
	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getUnitsPerEM()
	 */
	public int getUnitsPerEM() {
		return unitsPerEM;
	}


	/**
	 * Sets the units per em.
	 * 
	 * @param unitsPerEM the new units per em
	 */
	public void setUnitsPerEM(int unitsPerEM) {
		this.unitsPerEM = unitsPerEM;
	}

	/**
	 * Gets the font id.
	 * 
	 * @return the font id
	 */
	public String getFontId() {
		return fontId;
	}

	/**
	 * Sets the font id.
	 * 
	 * @param fontId the new font id
	 */
	public void setFontId(String fontId) {
		this.fontId = fontId;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFontFileName()
	 */
	public String getFontFileName() {
		return fontFileName;
	}


	/**
	 * Sets the font file name.
	 * 
	 * @param fontFileName the new font file name
	 */
	public void setFontFileName(String fontFileName) {
		this.fontFileName = fontFileName;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getOriginalFontSize()
	 */
	public int getOriginalFontSize() {
		return originalFontSize;
	}


	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getFillColor()
	 */
	public MTColor getFillColor() {
		return fillColor;
	}

	/* (non-Javadoc)
	 * @see org.mt4j.components.visibleComponents.font.IFont#getStrokeColor()
	 */
	public MTColor getStrokeColor() {
		return strokeColor;
	}



	

}
