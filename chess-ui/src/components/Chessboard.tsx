import React from 'react';
import styled from 'styled-components';
import PieceComponent from './PieceComponent';
import Size from '../ui/Size';
import SquareComponent from './SquareComponent';
import {squaresFromWhitePerspective} from '../domain/Square';
import {GameState} from '../domain/GameState';

const ChessboardContainer = styled.div`
  position: relative;
  width: ${_ => `${Size.squareSize * 8}px`};
  height: ${_ => `${Size.squareSize * 8}px`};
  display: grid;
  grid-template-columns: ${_ => `repeat(8, ${Size.squareSize}px)`};
  grid-template-rows: ${_ => `repeat(8, ${Size.squareSize}px)`};
`

const PiecesContainer = styled.div`
  height: 100%;
  width: 100%;
`
export type ChessboardProps = {
    state: GameState,
}
const Chessboard: React.FC<ChessboardProps> = (props) => {
    return (
        <ChessboardContainer>
            {squaresFromWhitePerspective.map((square) => (
                <SquareComponent square={square} key={square}></SquareComponent>
            ))}
            <PiecesContainer>
                {props.state.positions.map(position =>
                    <PieceComponent position={{square: position.square, piece: position.piece, color: position.color}} key={position.square}></PieceComponent>
                )}
            </PiecesContainer>
        </ChessboardContainer>
    )
}
export default Chessboard