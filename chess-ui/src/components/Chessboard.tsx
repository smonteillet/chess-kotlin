import React from 'react';
import styled from 'styled-components';
import PieceComponent from './PieceComponent';
import Size from '../ui/Size';
import SquareComponent from './SquareComponent';
import {squaresFromWhitePerspective} from '../domain/Square';

const ChessboardContainer = styled.div`
  position: relative;
  width: ${_ => `${Size.squareSize * 8}px`};
  height: ${_ => `${Size.squareSize * 8}px`};
  display: grid;
  margin: 0 auto;
  grid-template-columns: ${_ => `repeat(8, ${Size.squareSize}px)`};
  grid-template-rows: ${_ => `repeat(8, ${Size.squareSize}px)`};
`

const PiecesContainer = styled.div`
  height: 100%;
  width: 100%;
`

const Chessboard: React.FC = () => {

    const pieces = <PiecesContainer>
        <PieceComponent position={{square: 'a8', piece: 'rook', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'b8', piece: 'knight', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'c8', piece: 'bishop', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'd8', piece: 'king', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'e8', piece: 'queen', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'f8', piece: 'bishop', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'g8', piece: 'knight', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'h8', piece: 'rook', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'a7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'b7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'c7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'd7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'e7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'f7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'g7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'h7', piece: 'pawn', color: 'black'}}></PieceComponent>
        <PieceComponent position={{square: 'a1', piece: 'rook', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'b1', piece: 'knight', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'c1', piece: 'bishop', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'd1', piece: 'king', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'e1', piece: 'queen', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'f1', piece: 'bishop', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'g1', piece: 'knight', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'h1', piece: 'rook', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'a2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'b2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'c2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'd2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'e2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'f2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'g2', piece: 'pawn', color: 'white'}}></PieceComponent>
        <PieceComponent position={{square: 'h2', piece: 'pawn', color: 'white'}}></PieceComponent>
    </PiecesContainer>

    return (
        <ChessboardContainer>
            {squaresFromWhitePerspective.map((square) => (
                <SquareComponent square={square} key={square}></SquareComponent>
            ))}
            {pieces}
        </ChessboardContainer>
        // <div style={{
        //     display:"flex",
        //     width:"100px",
        //     height:"100px",
        //     backgroundColor:"#CCCCCC"}
        // }>
        //     {/*<div style={{display:"flex",alignItems:"flex-end",}}>H</div>*/}
        //     <div style={{
        //         display: "flex",
        //         alignItems : "flex-end",
        //         width: "50%",
        //     }}>1</div>
        //     <div style={{
        //         display: "flex",
        //         justifyContent : "flex-end",
        //         width: "50%",
        //     }}>1</div>
        // </div>
        // <div style={{position:"relative",width:"100px",height:"100px",backgroundColor:"#CCCCCC"}}>
        //     <div style={{position:"absolute", top:"0", right: "100"}}>1</div>
        //     <div style={{position:"absolute", top:"100", left: "0"}}>H</div>
        // </div>
    )
}
export default Chessboard