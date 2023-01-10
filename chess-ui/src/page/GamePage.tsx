import Chessboard from '../components/Chessboard';
import {GameState} from '../domain/GameState';

const GamePage = () => {
    const state : GameState = {
        positions : [
            {square: 'a8', piece: 'rook', color: 'black'},
            {square: 'b8', piece: 'knight', color: 'black'},
            {square: 'c8', piece: 'bishop', color: 'black'},
            {square: 'd8', piece: 'king', color: 'black'},
            {square: 'e8', piece: 'queen', color: 'black'},
            {square: 'f8', piece: 'bishop', color: 'black'},
            {square: 'g8', piece: 'knight', color: 'black'},
            {square: 'h8', piece: 'rook', color: 'black'},
            {square: 'a7', piece: 'pawn', color: 'black'},
            {square: 'b7', piece: 'pawn', color: 'black'},
            {square: 'c7', piece: 'pawn', color: 'black'},
            {square: 'd7', piece: 'pawn', color: 'black'},
            {square: 'e7', piece: 'pawn', color: 'black'},
            {square: 'f7', piece: 'pawn', color: 'black'},
            {square: 'g7', piece: 'pawn', color: 'black'},
            {square: 'h7', piece: 'pawn', color: 'black'},
            {square: 'a1', piece: 'rook', color: 'white'},
            {square: 'b1', piece: 'knight', color: 'white'},
            {square: 'c1', piece: 'bishop', color: 'white'},
            {square: 'd1', piece: 'king', color: 'white'},
            {square: 'e1', piece: 'queen', color: 'white'},
            {square: 'f1', piece: 'bishop', color: 'white'},
            {square: 'g1', piece: 'knight', color: 'white'},
            {square: 'h1', piece: 'rook', color: 'white'},
            {square: 'a2', piece: 'pawn', color: 'white'},
            {square: 'b2', piece: 'pawn', color: 'white'},
            {square: 'c2', piece: 'pawn', color: 'white'},
            {square: 'd2', piece: 'pawn', color: 'white'},
            {square: 'e2', piece: 'pawn', color: 'white'},
            {square: 'f2', piece: 'pawn', color: 'white'},
            {square: 'g2', piece: 'pawn', color: 'white'},
            {square: 'h2', piece: 'pawn', color: 'white'},
        ]
    }
    return <Chessboard state={state}></Chessboard>
}
export default GamePage