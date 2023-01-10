import {Square} from './Square';
import {Piece} from './Piece';
import {Color} from './Color';

export type Position = {
    square : Square,
    piece : Piece,
    color : Color,
}